package org.openbw.tsbw.building;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbw.bwapi4j.InteractionHandler;
import org.openbw.bwapi4j.MapDrawer;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.Color;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Addon;
import org.openbw.bwapi4j.unit.Building;

abstract class AddonProject implements Project {

	private static final Logger logger = LogManager.getLogger();
	
	private UnitType addonType;
	private TilePosition constructionSite;
	
	private int queuedGas;
	private int queuedMinerals;
	
	protected InteractionHandler interactionHandler;
	
	protected Addon addon;
	protected Building mainBuilding;
	
	protected AddonProject(UnitType addonType, Building mainBuilding, InteractionHandler interactionHandler) {
		
		this.addonType = addonType;
		this.mainBuilding = mainBuilding;
		this.constructionSite = new TilePosition(
				mainBuilding.getTilePosition().getX() + mainBuilding.tileWidth(),
				mainBuilding.getTilePosition().getY() + mainBuilding.tileHeight() - addonType.tileHeight());
		
		this.queuedMinerals = addonType.mineralPrice();
		this.queuedGas = addonType.gasPrice();
		this.interactionHandler = interactionHandler;
		this.addon = null;
	}
	
	@Override
	public void onFrame(Message message) {
		
		int minerals = message.getMinerals();
		int gas = message.getGas();
		
		if (this.addon == null && minerals >= addonType.mineralPrice() && gas >= addonType.gasPrice()) {
			
			construct();
		}
	}

	public void constructionStarted(Building building) {
		
		logger.debug("{}: Construction of {} has started.", this.interactionHandler.getFrameCount(), building);
		this.queuedGas = 0;
		this.queuedMinerals = 0;
		this.addon = (Addon) building;
	}

	abstract void construct();
	
	public int getQueuedGas() {
		
		return this.queuedGas;
	}
	
	public int getQueuedMinerals() {
		
		return this.queuedMinerals;
	}
	
	public boolean isConstructing(Building building) {
		
		if (building instanceof Addon) {
			
			Building main = ((Addon) building).getMainBuilding();
			return building.equals(this.addon) || this.mainBuilding == main;
		} else {
			
			return false;
		}
	}
	
	public boolean collidesWithConstruction(TilePosition position) {
		
		if (this.constructionSite != null) {
			
			if (this.constructionSite.getX() + this.addonType.tileWidth() > position.getX() &&  this.constructionSite.getX() < position.getX() + this.addonType.tileWidth()
					&& this.constructionSite.getY() + this.addonType.tileHeight() > position.getY() && this.constructionSite.getY() < position.getY() + this.addonType.tileHeight()) {
				
				return true;
			}
		}
		return false;
	}
	
	public boolean isDone() {
		
		return this.addon != null && this.addon.isCompleted();
	}
	
	public void drawConstructionSite(MapDrawer mapDrawer) {
		
		if (this.constructionSite != null) {
			mapDrawer.drawBoxMap(this.constructionSite.getX() * 32, this.constructionSite.getY() * 32, 
					this.constructionSite.getX() * 32 + this.addonType.tileWidth() * 32, 
					this.constructionSite.getY() * 32 + this.addonType.tileHeight() * 32, Color.WHITE);
		}
	}
	
	public boolean isOfType(ConstructionType constructionType) {
		
		return this.addonType.equals(constructionType);
	}
}
