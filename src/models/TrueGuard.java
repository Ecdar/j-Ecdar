package models;

import java.util.Objects;

public class TrueGuard extends Guard{

    @Override
    int getMaxConstant() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof TrueGuard)
            return true;
        return false;
    }

    @Override
    public String toString() {
        return "true";
    }

    @Override
    public int hashCode() {
        return Objects.hash(false);
    }
}
