package models;

public abstract class UniqueNamed {
    protected String originalName;
    protected String uniqueName;
    protected String ownerName;

    public abstract UniqueNamed getCopy();

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
