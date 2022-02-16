package models;

import lib.CDDLib;

import java.util.Objects;

public class CDD {

    private long pointer;

    private CDD(){
        this.pointer = CDDLib.allocateCdd();
    }

    private CDD(long pointer){
        this.pointer = pointer;
    }

    public static int init(int maxSize, int cs, int stackSize) {
        return CDDLib.cddInit(maxSize, cs, stackSize);
    }

    public static void addClocks(int amount){
        CDDLib.cddAddClocks(amount);
    }

    public static int addBddvar(int amount) { return CDDLib.addBddvar(amount); }

    public static CDD allocate(){
        return new CDD();
    }

    public static CDD allocateInterval(int i, int j, int lower, int upper){
        return new CDD(CDDLib.interval(i,j,lower,upper));
    }

    public static CDD allocateFromDbm(int[] dbm, int dim){
        return new CDD(CDDLib.cddFromDbm(dbm, dim));
    }

    public static CDD allocateLower(int i, int j, int lowerBound) {
        return new CDD(CDDLib.lower(i,j,lowerBound));
    }

    public static CDD allocateUpper(int i, int j, int upperBound) {
        return new CDD(CDDLib.upper(i,j,upperBound));
    }

    public static CDD createBddNode(int level) {
        return new CDD(CDDLib.cddBddvar(level));
    }

    public static CDD createNegatedBddNode(int level) {
        return new CDD(CDDLib.cddNBddvar(level));
    }

    public static void free(CDD cdd){
        CDDLib.freeCdd(cdd.pointer);
        cdd.pointer = 0;
    }

    public CDD conjunction(CDD other){
        long resultPointer = CDDLib.conjunction(pointer, other.pointer);
        return new CDD(resultPointer);
    }

    public CDD disjunction(CDD other){
        long resultPointer = CDDLib.disjunction(pointer, other.pointer);
        return new CDD(resultPointer);
    }

    public CDD negation() {
        long resultPointer = CDDLib.negation(pointer);
        return new CDD(resultPointer);
    }

    public CDD reduce() {
        long resultPointer = CDDLib.reduce(pointer);
        return new CDD(resultPointer);
    }

    public int getNodeCount(){
        return CDDLib.cddNodeCount(pointer);
    }

    public CDDNode getRoot(){
        long nodePointer = CDDLib.getRootNode(this.pointer);
        return new CDDNode(nodePointer);
    }

    public void printDot() {
        CDDLib.cddPrintDot(pointer);
    }

    public void printDot(String filePath){
        CDDLib.cddPrintDot(pointer, filePath);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CDD cdd = (CDD) o;
        return pointer == cdd.pointer;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pointer);
    }
}
