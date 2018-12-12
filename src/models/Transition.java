package models;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Transition {

		private Location source;
		private Location target;
		private Channel chan;
		private boolean isInput;
		private List<Guard> guards;
		private List<Update> updates;

		public Transition(Location source, Location target, Channel chan, boolean isInput, List<Guard> guards, List<Update> updates) {
				this.source = source;
				this.target = target;
				this.chan = chan;
				this.isInput = isInput;
				this.guards = guards;
				this.updates = updates;
		}

		public Location getSource() {
				return source;
		}

		public Location getTarget() {
				return target;
		}

		public Channel getChannel() {
				return chan;
		}

		public boolean isInput() {
				return isInput;
		}

		public List<Guard> getGuards() {
				return guards;
		}

		public List<Update> getUpdates() {
				return updates;
		}

		@Override
		public boolean equals(Object o) {
				if (this == o) return true;
				if (!(o instanceof Transition)) return false;
				Transition that = (Transition) o;
				return isInput == that.isInput &&
								source.equals(that.source) &&
								target.equals(that.target) &&
								chan.equals(that.chan) &&
								Arrays.equals(guards.toArray(), that.guards.toArray()) &&
								Arrays.equals(updates.toArray(), that.updates.toArray());
		}

		@Override
		public int hashCode() {
				return Objects.hash(source, target, chan, isInput, guards, updates);
		}
}
