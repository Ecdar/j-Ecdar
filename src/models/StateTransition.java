package models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class StateTransition {
		private State from;
		private State to;
		private Channel channel;
		private boolean isInput;
		private ArrayList<Guard> guards;
		private ArrayList<Update> updates;

		public StateTransition(State from, State to, Channel channel, boolean isInput, ArrayList<Guard> guards, ArrayList<Update> updates) {
				this.from = from;
				this.to = to;
				this.channel = channel;
				this.isInput = isInput;
				this.guards = guards;
				this.updates = updates;
		}

		public State getFrom() {
				return from;
		}

		public void setFrom(State from) {
				this.from = from;
		}

		public State getTo() {
				return to;
		}

		public void setTo(State to) {
				this.to = to;
		}

		public Channel getChannel() {
				return channel;
		}

		public void setChannel(Channel channel) {
				this.channel = channel;
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
		public boolean equals(Object o) {
				if (this == o) return true;
				if (o == null || getClass() != o.getClass()) return false;
				StateTransition that = (StateTransition) o;
				return isInput == that.isInput &&
								from.equals(that.from) &&
								to.equals(that.to) &&
								channel.equals(that.channel) &&
								Arrays.equals(guards.toArray(), that.guards.toArray()) &&
								Arrays.equals(updates.toArray(), that.updates.toArray());
		}

		@Override
		public int hashCode() {
				return Objects.hash(from, to, channel, isInput, guards, updates);
		}
}
