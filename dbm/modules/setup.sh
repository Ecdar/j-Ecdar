#!/usr/bin/env bash
#
# Filename: setup.sh
#
# Setup compilation (.config, configure, build directory).
#
# Variables to set to avoid being prompted:
# BUILD_DIR
# INSTALL_DIR
# COMPILER
# OPTIONS
# UPPAAL_FLAGS
#
# It is possible to define your own CFLAGS that will be added
# to the other options.

SRC_TAG="source"
BUILD_TAG="build"
CC_TAG="cc"
CXX_TAG="cxx"

# Check version info
check_autogen() {
    . version.sh 0
    if [ ! -f configure.ac -o "`grep 'AC_SUBST(TIGA_VERSION' configure.ac | grep -o 'rev. [[:digit:]]*'`" != "$REVISION" ]; then
	echo "Autogen..."
        ./autogen.sh
    fi
}

# Read a given option ($1), separator is ':'
read_option() { grep $1 "$CONFIG" | cut -d: -f2; }

# Read configuration information
read_config() {
    cfg_sourcedir=`pwd`
    if [ "`basename $cfg_sourcedir`" != "modules" ]; then
	echo "Error: the source directory must be 'modules'" 1>&2
	exit 1
    fi
    CONFIG="$cfg_sourcedir/.config"
    if [ -f "$CONFIG" ]; then
	cfg_builddir=`read_option "$BUILD_TAG"`
	cfg_cc=`read_option "$CC_TAG"`
	cfg_cxx=`read_option "$CXX_TAG"`
	if [ -z "$cfg_builddir" -o ! -d "$cfg_builddir" ]; then
	    echo "Default configuration outdated: $cfg_builddir not found!" 1>&2
	fi
    fi
}

# Write configuration information
write_config() {
    echo "# GENERATED CONFIGURATION" >  "$CONFIG"
    echo "$SRC_TAG:$cfg_sourcedir"   >> "$CONFIG"
    echo "$BUILD_TAG:$cfg_builddir"  >> "$CONFIG"
    echo "$CC_TAG:$cfg_cc"           >> "$CONFIG"
    echo "$CXX_TAG:$cfg_cxx"         >> "$CONFIG"
}

# Get value or default value
get_default() { test -z "$2" && echo $1 || echo $2; }

# Read build directory
read_builddir() {
    if [ -z "$BUILD_DIR" ]; then
      default_value=`get_default $cfg_sourcedir $cfg_builddir`
      echo "Build directory [$default_value]"
      read builddir
      cfg_builddir=`get_default $default_value $builddir`
    else
      cfg_builddir="$BUILD_DIR"
      echo "Build directory: $builddir"
    fi
    # Expand the directory name.
    cfg_builddir=`eval echo $cfg_builddir`
    # Try to expand ~/
    if [ ! -z "$HOME" ]; then
	cfg_builddir=`echo $cfg_builddir | sed s:^~/:$HOME/:g`
    fi
    if [ ! -z "`echo $cfg_builddir | grep '~'`" ]; then
	echo "Cannot expand '~'" 1>&2
	exit 1
    fi
    # Expand . to current directory.
    if [ "$cfg_builddir" == "." ]; then
	cfg_builddir=`pwd`
    fi
    if [ -z "`echo $cfg_builddir | grep '^/'`" ]; then
	cfg_builddir="$cfg_sourcedir/$cfg_builddir"
	echo "*** Warning: relative path converted to absolute path" 1>&2
	echo "*** $cfg_builddir" 1>&2
    fi
}

# Choose install path (DBM).
read_prefix() {
    if [ -z "$INSTALL_DIR" ]; then
      echo "Install path (only for the DBM library) [/usr/local]:"
      read p
    else
      PREFIX="--prefix=$INSTALL_DIR"
      echo "Install path: $INSTALL_DIR"
    fi
    if [ ! -z "$p" ]; then
	PREFIX="--prefix=$p"
    fi
}

# Choose a valid value among possible choices ($*), or nothing
choose() {
    loop=1
    while [ $loop = 1 ]; do
	read choice
	if [ -z "$choice" ]; then
	    loop=0
	else
	    for i in $*; do
		if [ "$i" = "$choice" ]; then
		    loop=0
		fi
	    done
	fi
    done
}

# Choose compiler
choose_cc() {
    cd ../compilers || { echo "Bad path: ../compilers not found" 1>&2; exit 1; }
    if [ -z "$COMPILER" ]; then
        ccc=
        for i in `ls`; do
	    if [ -d "$i" ]; then
	        if which $i >/dev/null; then
		    ccc="$ccc $i"
		    if [ -z "$firstcc" ]; then
		        firstcc="$i"
		    fi
	        fi
	    fi
        done
        if [ -z "$ccc" ]; then
	    echo "No compiler available"
    	    exit 1
	fi
	echo "Available compilers:"
	echo
	if [ `echo $ccc | wc -w` = 1 ]; then
	    echo "Only $ccc available!"
	    echo
	    cc=$ccc
	else
	    for i in $ccc; do echo " $i"; done
	    if [ ! -z "$cfg_cxx" -a -d "$cfg_cxx" ]; then
		default_value="$cfg_cxx"
	    else
		default_value="$firstcc"
	    fi
	    echo 
	    echo "Choose compiler [$default_value]:"
	    choose $ccc
	    cc=`get_default $default_value $choice`
	fi
    else
	cc="$COMPILER"
	echo "Compiler: $COMPILER"
    fi
    cc=`echo $cc | sed 's/^ *//g'`
    cfg_cc=${cc/g++/gcc}
    cfg_cxx=${cc/gcc/g++}
}

# Choose compiler flags
choose_cflags() {
    cd $cc || { echo "Fatal: chdir $cc." 2>&1; exit 1; }
    if [ -z "$OPTIONS" ]; then
	default=`cat .lastcflags 2>/dev/null`
	default=`get_default 1 "$default"`
	test -f options || { echo "Fatal: options not found." 2>&1; exit 1; }
	echo "Compilation options for $cc:"
	cat options | sed -e "s:@.*::g" -e "s:%.*::g"
	echo
	echo "Choose (e.g. 1 2 3) [$default]:"
	read choice
	flagschoice=`get_default "$default" "$choice"`
	echo $flagschoice > .lastcflags
    else
	flagschoice="$OPTIONS"
    fi
    cfg_cflags=`cat options | grep '%' | sed "s:^%::g"`
    for a in $flagschoice; do
	opts=`grep "^ *$a *:" options`
    	cfg_cflags="$cfg_cflags `echo $opts | cut -d@ -f2`"
	cfg_lflags="$cfg_lflags `echo $opts | cut -d@ -f3`"
    done
    if [ `uname` = "Darwin" ]; then
        cfg_lflags="${cfg_lflags/-static/}"
    fi
    cd ..
    if [ -z "$UPPAAL_FLAGS" ]; then
	default=`cat .lastflags 2>/dev/null`
	echo "Configuration options:"
	cat ../scripts/cflags.txt | sed "s@:.*:@:@g"
	echo
	echo "Choose (e.g. 1 2 3) [$default]:"
	read choice
	choice=`get_default "$default" "$choice"`
	echo $choice > .lastflags
    else
	choice="$UPPAAL_FLAGS"
    fi
    for i in $choice; do
	cflags="$cflags `grep -h "^ *$i *:" ../scripts/cflags.txt .lastflags 2>/dev/null | cut -d: -f2`"
    done
    cfg_cflags=`echo $cfg_cflags $cflags | sed 's: +: :g'`
}

# --------------------------------- main --------------------------------------

check_autogen
which makedepend >/dev/null || echo "Warning: you need makedepend to compile!" 1>&2
read_config
read_builddir
read_prefix
choose_cc
choose_cflags
write_config
echo "Configuring..."
#cd ../modules && ./autogen.sh
install -d $cfg_builddir || exit 1
cd "$cfg_builddir" || exit 1
if [ "$cfg_cc" == i586-mingw32msvc-gcc ]; then
    cfg_ar="i586-mingw32msvc-ar"
    cfg_strip="i586-mingw32msvc-strip"
    EXTRAHOST="--host=i586-pc-mingw32msvc"
elif [ "$cfg_cc" == amd64-mingw32msvc-gcc ]; then
    cfg_ar="amd64-mingw32msvc-ar"
    cfg_strip="amd64-mingw32msvc-strip"
    EXTRAHOST="--host=amd64-pc-mingw32msvc"
elif [ "$cfg_cc" == llvm-gcc ]; then
    cfg_ar="llvm-ar"
    cfg_strip="strip"
else
    cfg_ar="ar"
    cfg_strip="strip"
fi
LINKFLAGS="$cfg_lflags" CFLAGS="$cfg_cflags $CFLAGS" CC="$cfg_cc" CXX="$cfg_cxx" AR="$cfg_ar" STRIP="$cfg_strip" "$cfg_sourcedir"/configure $PREFIX $EXTRAHOST
