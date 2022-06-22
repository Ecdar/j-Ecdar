package models;

import java.util.List;
import java.util.Objects;

public abstract class Update {



    @Override
    public abstract boolean equals(Object o) ;
    @Override
    public abstract String toString() ;

    @Override
    public abstract int hashCode() ;
    /*
    private final Clock clock;
    private final int value;

    public Update(Clock clock, int value) {
        this.clock = clock;
        this.value = value;
    }

    public Update(Update copy, List<Clock> clocks){
        this.clock = clocks.get(clocks.indexOf(copy.clock));
        this.value = copy.value;
    }

    public Clock getClock() {
        return clock;
    }

    public int getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Update)) return false;
        Update update = (Update) o;
        return value == update.value &&
                clock.equals(update.clock);
    }

    @Override
    public String toString() {
        return "Update{" +
                "clock=" + clock +
                ", value=" + value +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(clock, value);
    }*/
}
