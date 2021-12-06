package com.ntscorp.auto_client.data;

import java.util.HashMap;

public class DeviceSize {

	public HashMap<String, value> size = new HashMap<>();
	
	public DeviceSize() {
		setDeviceList();
	}
	
	public enum value {
		LG_G2("LGF320S", 360, 640),
		LG_G3("LGF400S", 360, 640),
		LG_G4("LGF500S", 360, 640),
		LG_G5("LGF700S", 360, 640),
		LG_G6("LGMG600S", 360, 640),
		LG_V10("LGF600S", 360, 640),
		LG_V20("LGF800S", 360, 680),
		NEXUS_5("Nexus5", 360, 640),
		NEXUS_5X("Nexus5X", 412, 731),
		NEXUS_6("Nexus6", 412, 731),
		NEXUS_6P("Nexus6P", 412, 731),
		SAMSUNG_GALAXY_NOTE_3("SMN900S", 360, 640),
		SAMSUNG_GALAXY_NOTE_4_SKT("SMN910S", 360, 640),
		SAMSUNG_GALAXY_NOTE_4_KT("SMN910K", 360, 640),
		SAMSUNG_GALAXY_NOTE_5("SMN920S", 411, 731),
		SAMSUNG_GALAXY_S4("SHVE300S", 360, 640),
		SAMSUNG_GALAXY_S5("SMG900S", 360, 640),
		SAMSUNG_GALAXY_S6("SMG920S", 360, 640),
		SAMSUNG_GALAXY_S6_EDGE("SMG925S", 360, 640),
		SAMSUNG_GALAXY_S6_EDGE_PLUS("SMG928S", 360, 640),
		SAMSUNG_GALAXY_S7("SMG930S", 360, 640),
		SAMSUNG_GALAXY_S7_EDGE("SMG935S", 411, 731),
		SAMSUNG_GALAXY_S8("SMG950N", 360, 740),
		SAMSUNG_GALAXY_S8_PLUS("SMG955N", 360, 740);
		
		private String modelName = "";
		private int viewportX = 0;
		private int viewportY = 0;
		
		private value(String name, int width, int height) {
			setModelName(name);
			setViewportX(width);
			setViewportY(height);
		}
		
		public String getModelName() {
			return modelName;
		}

		public void setModelName(String modelName) {
			this.modelName = modelName;
		}

		public int getViewportX() {
			return viewportX;
		}

		public void setViewportX(int viewportX) {
			this.viewportX = viewportX;
		}

		public int getViewportY() {
			return viewportY;
		}

		public void setViewportY(int viewportY) {
			this.viewportY = viewportY;
		}
	}
	
	public void setDeviceList() {
		size.put(value.LG_G2.getModelName(), value.LG_G2);
		size.put(value.LG_G3.getModelName(), value.LG_G3);
		size.put(value.LG_G4.getModelName(), value.LG_G4);
		size.put(value.LG_G5.getModelName(), value.LG_G5);
		size.put(value.LG_G6.getModelName(), value.LG_G6);
		size.put(value.LG_V10.getModelName(), value.LG_V10);
		size.put(value.LG_V20.getModelName(), value.LG_V20);
		size.put(value.NEXUS_5.getModelName(), value.NEXUS_5);
		size.put(value.NEXUS_5X.getModelName(), value.NEXUS_5X);
		size.put(value.NEXUS_6.getModelName(), value.NEXUS_6);
		size.put(value.NEXUS_6P.getModelName(), value.NEXUS_6P);
		size.put(value.SAMSUNG_GALAXY_NOTE_3.getModelName(), value.SAMSUNG_GALAXY_NOTE_3);
		size.put(value.SAMSUNG_GALAXY_NOTE_4_SKT.getModelName(), value.SAMSUNG_GALAXY_NOTE_4_SKT);
		size.put(value.SAMSUNG_GALAXY_NOTE_4_KT.getModelName(), value.SAMSUNG_GALAXY_NOTE_4_KT);
		size.put(value.SAMSUNG_GALAXY_NOTE_5.getModelName(), value.SAMSUNG_GALAXY_NOTE_5);
		size.put(value.SAMSUNG_GALAXY_S4.getModelName(), value.SAMSUNG_GALAXY_S4);
		size.put(value.SAMSUNG_GALAXY_S5.getModelName(), value.SAMSUNG_GALAXY_S5);
		size.put(value.SAMSUNG_GALAXY_S6.getModelName(), value.SAMSUNG_GALAXY_S6);
		size.put(value.SAMSUNG_GALAXY_S6_EDGE.getModelName(), value.SAMSUNG_GALAXY_S6_EDGE);
		size.put(value.SAMSUNG_GALAXY_S6_EDGE_PLUS.getModelName(), value.SAMSUNG_GALAXY_S6_EDGE_PLUS);
		size.put(value.SAMSUNG_GALAXY_S7.getModelName(), value.SAMSUNG_GALAXY_S7);
		size.put(value.SAMSUNG_GALAXY_S7_EDGE.getModelName(), value.SAMSUNG_GALAXY_S7_EDGE);
		size.put(value.SAMSUNG_GALAXY_S8.getModelName(), value.SAMSUNG_GALAXY_S8);
		size.put(value.SAMSUNG_GALAXY_S8_PLUS.getModelName(), value.SAMSUNG_GALAXY_S8_PLUS);
	}
}