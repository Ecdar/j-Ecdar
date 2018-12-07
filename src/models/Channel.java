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

		@Override
		public int hashCode() {
				return Objects.hash(name);
		}
}
