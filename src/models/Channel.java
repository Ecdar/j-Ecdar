package models;

import java.util.Objects;

public class Channel {
		private String name;

		public Channel(String name) {
				this.name = name;
		}

		public String getName() {
				return name;
		}

		public void setName(String name) {
				this.name = name;
		}

		@Override
		public String toString() {
				return "Channel " + name;
		}

		@Override
		public boolean equals(Object o) {
				if (this == o) return true;
				if (o == null || getClass() != o.getClass()) return false;
				Channel channel = (Channel) o;
				return name == channel.name;
		}

		@Override
		public int hashCode() {
				return Objects.hash(name);
		}
}
