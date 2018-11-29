package models;

import java.util.ArrayList;
import java.util.Arrays;

public class Transition {

		private Location source;
		private Location target;
		private Channel chan;
		private boolean isInput;
		private ArrayList<Guard> guards;
		private ArrayList<Update> updates;

		public Transition(Location source, Location target, Channel chan, boolean isInput, ArrayList<Guard> guards, ArrayList<Update> updates) {
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

		public void setSource(Location source) {
				this.source = source;
		}

		public Location getTarget() {
				return target;
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

		public ArrayList<Update> getUpdates() {
				return updates;
		}

		public Update getUpdate() { return (updates.size() == 0) ? null : updates.get(0); }

		@Override
		public String toString() {
				return "Transition{" +
								"source=" + source +
								", target=" + target +
								", chan=" + chan +
								", isInput=" + isInput +
								", guards=" + guards +
								", updates=" + updates +
								'}';
		}
}
