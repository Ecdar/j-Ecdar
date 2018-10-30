package models;

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
}
