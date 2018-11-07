package models;

import java.util.ArrayList;
import java.util.Arrays;

public class Transition {

		private Location from;
		private Location to;
		private Channel chan;
		private boolean isInput;
		private ArrayList<Guard> guards;
		private ArrayList<Update> updates;

		public Transition(Location from, Location to, Channel chan, boolean isInput, Guard guard, Update update) {
				this.from = from;
				this.to = to;
				this.chan = chan;
				this.isInput = isInput;
				this.guards = new ArrayList<>();
				if (guard != null) guards.add(guard);
				this.updates = new ArrayList<>();
				if (update != null) updates.add(update);
		}

		public Transition(Location from, Location to, Channel chan, boolean isInput, ArrayList<Guard> guards, ArrayList<Update> updates) {
				this.from = from;
				this.to = to;
				this.chan = chan;
				this.isInput = isInput;
				this.guards = guards;
				this.updates = updates;
		}

		public Location getFrom() {
				return from;
		}

		public void setFrom(Location from) {
				this.from = from;
		}

		public Location getTo() {
				return to;
		}

		public void setTo(Location to) {
				this.to = to;
		}

		public Channel getChannel() {
				return chan;
		}

		public void setChannel(Channel chan) {
				this.chan = chan;
		}

		public boolean isInput() {
				return isInput;
		}

		public void setInput(boolean input) {
				isInput = input;
		}

		public ArrayList<Guard> getGuards() {
				return guards;
		}

		public Guard getGuard() { return (guards.size() == 0) ? null : guards.get(0); }

		public void setGuards(ArrayList<Guard> guards) {
				this.guards = guards;
		}

		public ArrayList<Update> getUpdates() {
				return updates;
		}

		public Update getUpdate() { return (updates.size() == 0) ? null : updates.get(0); }

		public void setUpdates(ArrayList<Update> updates) {
				this.updates = updates;
		}

		@Override
		public String toString() {
				return "Transition{" +
								"from=" + from +
								", to=" + to +
								", chan=" + chan +
								", isInput=" + isInput +
								", guards=" + guards +
								", updates=" + updates +
								'}';
		}
}
