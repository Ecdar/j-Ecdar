package models;

import java.util.Objects;

public class FalseGuard extends Guard{

    @Override
    int getMaxConstant() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof FalseGuard)
            return true;
        return false;
    }

    @Override
    public String toString() {
        return "false";
    }

    @Override
    public int hashCode() {
        return Objects.hash(false);
    }
}
