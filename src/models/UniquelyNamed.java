package models;

public abstract class UniquelyNamed {
    protected String originalName;
    protected String uniqueName;
    protected String ownerName;

    public abstract UniquelyNamed getCopy();

    public String getOriginalName() {
        return originalName;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public void setUniqueName(int index) {
        if (ownerName != null) {
            uniqueName = ownerName + "." + index + "." + originalName;
        }
    }

    public void setUniqueName() {
        if (ownerName != null) {
            uniqueName = ownerName + "." + originalName;
        }
    }

    public String getOwnerName() {
        return ownerName;
    }
}
