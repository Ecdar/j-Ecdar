/* -*- mode: C++; c-file-style: "stroustrup"; c-basic-offset: 4; -*-
 *
 * This file is part of the UPPAAL DBM library.
 *
 * The UPPAAL DBM library is free software; you can redistribute it
 * and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version
 * 2 of the License, or (at your option) any later version.
 *
 * The UPPAAL DBM library is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with the UPPAAL DBM library; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
 * Boston, MA  02110-1301  USA.
 */

// -*- mode: C++; c-file-style: "stroustrup"; c-basic-offset: 4; indent-tabs-mode: nil; -*-
////////////////////////////////////////////////////////////////////
//
// This file is a part of the UPPAAL toolkit.
// Copyright (c) 1995 - 2006, Uppsala University and Aalborg University.
// All right reserved.
//
///////////////////////////////////////////////////////////////////

#include <string>
#include <string.h>
#include <iostream>
#include "io/Connection.h"

// #define VERBOSE

#define REPLY_MSG "<html><body>You are being <a href=\"http://127.0.0.1:"
#define REPLY_LENGTH (sizeof(REPLY_MSG)-1)

using io::Connection;

std::string get_license(const char* key)
{
    Connection link("bugsy.grid.aau.dk", 80);
    std::string buffer("POST /lisa/licenses/");
    buffer += key;
    buffer += "\n";

    if (!link.isOpen())
    {
        std::cerr << "Connection failed.\n";
        return std::string();
    }

#ifdef VERBOSE
    std::cerr << "write: " << buffer << std::endl;
#endif

    if (!link.write(buffer))
    {
        std::cerr << "Write1 failed.\n";
        return std::string();
    }

    char *str = (char*) link.read();
    if (!str)
    {
        std::cerr << "Read failed.\n";
        return std::string();
    }

#ifdef VERBOSE
    std::cerr << "read: " << str << std::endl;
#endif

    if (strncmp(str, REPLY_MSG, REPLY_LENGTH) != 0)
    {
        std::cerr << str << std::endl;
        return std::string();
    }

    // Hack.
    char *a = &str[REPLY_LENGTH];
    a[0] = 'G';
    a[1] = 'E';
    a[2] = 'T';
    a[3] = ' ';
    for(char *b = a+4; *b != 0; ++b)
    {
        if (strncmp(b, "\">", 2) == 0)
        {
            *b++ = '\n';
            *b = 0;
        }
    }

    if (!link.reconnect())
    {
        std::cerr << "Reconnection failed.\n";
        return std::string();
    }

#ifdef VERBOSE
    std::cerr << "write: " << a << std::endl;
#endif

    if (!link.write(a, strlen(a)))
    {
        std::cerr << "Write2 failed.\n";
        return std::string();
    }

    str = (char*) link.read();
    if (!str)
    {
        std::cerr << "Read failed.\n";
        return std::string();
    }
    
#ifdef VERBOSE
    std::cerr << "read: " << str << std::endl;
#endif

    return str;
}


// Test key: 1fefcce3-82f8-7ecf-3ddb-b1fd171d

int main(int argc, char *argv[])
{
    if (argc < 2)
    {
        std::cerr << "Usage: " << argv[0]
                  << " key\n";
        return 1;
    }

    std::cout << get_license(argv[1]) << std::endl;

    return 0;
}
