package net.minecraft.src.buildcraft.transport;

import java.util.LinkedList;

import net.minecraft.src.BuildCraftTransport;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;
import net.minecraft.src.mod_BuildCraftTransport;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.api.Action;
import net.minecraft.src.buildcraft.api.IPipe;
import net.minecraft.src.buildcraft.api.Trigger;
import net.minecraft.src.buildcraft.api.power.IPowerReceptor;
import net.minecraft.src.buildcraft.core.DefaultProps;
import net.minecraft.src.buildcraft.core.GuiIds;
import net.minecraft.src.buildcraft.core.Utils;

public class GateVanilla extends Gate {

	private EnergyPulser pulser;

	public GateVanilla(Pipe pipe) {
		super(pipe);
	}

	public GateVanilla(Pipe pipe, ItemStack stack) {
		super(pipe, stack);

		if (stack.itemID == BuildCraftTransport.pipeGateAutarchic.shiftedIndex)
			addEnergyPulser(pipe);
	}

	// / SAVING & LOADING
	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		if (pulser != null) {
			NBTTagCompound nbttagcompoundC = new NBTTagCompound();
			pulser.writeToNBT(nbttagcompoundC);
			nbttagcompound.setTag("Pulser", nbttagcompoundC);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

		// Load pulser if any
		if (nbttagcompound.hasKey("Pulser")) {
			NBTTagCompound nbttagcompoundP = nbttagcompound.getCompoundTag("Pulser");
			pulser = new EnergyPulser((IPowerReceptor) pipe);
			pulser.readFromNBT(nbttagcompoundP);
		}

	}

	// GUI
	@Override
	public void openGui(EntityPlayer player) {
		if (!APIProxy.isClient(player.worldObj))
			player.openGui(mod_BuildCraftTransport.instance, GuiIds.GATES, pipe.worldObj, pipe.xCoord, pipe.yCoord, pipe.zCoord);
	}

	// / UPDATING
	@Override
	public void update() {
		if (hasPulser())
			pulser.update();
	}

	// / INFORMATION
	private boolean hasPulser() {
		return pulser != null;
	}

	@Override
	public String getName() {

		switch (kind) {
		case Single:
			return "Gate";
		case AND_2:
			return "Iron AND Gate";
		case AND_3:
			return "Golden AND Gate";
		case AND_4:
			return "Diamond AND Gate";
		case OR_2:
			return "Iron OR Gate";
		case OR_3:
			return "Golden OR Gate";
		case OR_4:
			return "Diamond OR Gate";
		}

		return "";
	}

	@Override
	public GateConditional getConditional() {
		if (kind == GateKind.OR_2 || kind == GateKind.OR_3 || kind == GateKind.OR_4)
			return GateConditional.OR;
		else if (kind == GateKind.AND_2 || kind == GateKind.AND_3 || kind == GateKind.AND_4)
			return GateConditional.AND;
		else
			return GateConditional.None;
	}

	/**
	 * Tries to add an energy pulser to gates that accept energy.
	 * 
	 * @param pipe
	 * @return
	 */
	private boolean addEnergyPulser(Pipe pipe) {
		if (!(pipe instanceof IPowerReceptor))
			return false;

		pulser = new EnergyPulser((IPowerReceptor) pipe);

		return true;
	}

	/**
	 * Drops a gate item of the specified kind.
	 * 
	 * @param kind
	 * @param world
	 * @param i
	 * @param j
	 * @param k
	 */
	@Override
	public void dropGate(World world, int i, int j, int k) {

		int gateDamage = 0;
		switch (kind) {
		case Single:
			gateDamage = 0;
			break;
		case AND_2:
			gateDamage = 1;
			break;
		case OR_2:
			gateDamage = 2;
			break;
		case AND_3:
			gateDamage = 3;
			break;
		case OR_3:
			gateDamage = 4;
			break;
		case AND_4:
			gateDamage = 5;
			break;
		case OR_4:
			gateDamage = 6;
			break;
		}

		Item gateItem;
		if (hasPulser())
			gateItem = BuildCraftTransport.pipeGateAutarchic;
		else
			gateItem = BuildCraftTransport.pipeGate;

		Utils.dropItems(world, new ItemStack(gateItem, 1, gateDamage), i, j, k);

	}

	// / ACTIONS
	@Override
	public void addActions(LinkedList<Action> list) {

		if (pipe.wireSet[IPipe.WireColor.Red.ordinal()] && kind.ordinal() >= Gate.GateKind.AND_2.ordinal())
			list.add(BuildCraftTransport.actionRedSignal);

		if (pipe.wireSet[IPipe.WireColor.Blue.ordinal()] && kind.ordinal() >= Gate.GateKind.AND_3.ordinal())
			list.add(BuildCraftTransport.actionBlueSignal);

		if (pipe.wireSet[IPipe.WireColor.Green.ordinal()] && kind.ordinal() >= Gate.GateKind.AND_4.ordinal())
			list.add(BuildCraftTransport.actionGreenSignal);

		if (pipe.wireSet[IPipe.WireColor.Yellow.ordinal()] && kind.ordinal() >= Gate.GateKind.AND_4.ordinal())
			list.add(BuildCraftTransport.actionYellowSignal);

		if (hasPulser())
			list.add(BuildCraftTransport.actionEnergyPulser);

	}

	@Override
	public void startResolution() {
		if (hasPulser())
			pulser.disablePulse();
	}

	@Override
	public boolean resolveAction(Action action) {

		if (action instanceof ActionEnergyPulser) {
			pulser.enablePulse();
			return true;
		}

		return false;
	}

	// / TRIGGERS
	@Override
	public void addTrigger(LinkedList<Trigger> list) {

		if (pipe.wireSet[IPipe.WireColor.Red.ordinal()] && kind.ordinal() >= Gate.GateKind.AND_2.ordinal()) {
			list.add(BuildCraftTransport.triggerRedSignalActive);
			list.add(BuildCraftTransport.triggerRedSignalInactive);
		}

		if (pipe.wireSet[IPipe.WireColor.Blue.ordinal()] && kind.ordinal() >= Gate.GateKind.AND_3.ordinal()) {
			list.add(BuildCraftTransport.triggerBlueSignalActive);
			list.add(BuildCraftTransport.triggerBlueSignalInactive);
		}

		if (pipe.wireSet[IPipe.WireColor.Green.ordinal()] && kind.ordinal() >= Gate.GateKind.AND_4.ordinal()) {
			list.add(BuildCraftTransport.triggerGreenSignalActive);
			list.add(BuildCraftTransport.triggerGreenSignalInactive);
		}

		if (pipe.wireSet[IPipe.WireColor.Yellow.ordinal()] && kind.ordinal() >= Gate.GateKind.AND_4.ordinal()) {
			list.add(BuildCraftTransport.triggerYellowSignalActive);
			list.add(BuildCraftTransport.triggerYellowSignalInactive);
		}

	}

	// / TEXTURES
	@Override
	public final int getTexture(boolean isSignalActive) {

		boolean isGateActive = isSignalActive;
		if (hasPulser() && pulser.isActive())
			isGateActive = true;

		int n = getTextureRow();
		switch (kind) {
		case None:
			break;
		case Single:
			if (!isGateActive)
				return n * 16 + 12;
			else
				return n * 16 + 13;
		case AND_2:
			if (!isGateActive) {
				if (hasPulser())
					return 9 * 16 + 0;
				else
					return 6 * 16 + 7;
			} else if (hasPulser())
				return 9 * 16 + 1;
			else
				return 6 * 16 + 8;
		case OR_2:
			if (!isGateActive) {
				if (hasPulser())
					return 9 * 16 + 2;
				else
					return 6 * 16 + 9;
			} else if (hasPulser())
				return 9 * 16 + 3;
			else
				return 6 * 16 + 10;
		case AND_3:
			if (!isGateActive)
				return n * 16 + 4;
			else
				return n * 16 + 5;
		case OR_3:
			if (!isGateActive)
				return n * 16 + 6;
			else
				return n * 16 + 7;
		case AND_4:
			if (!isGateActive)
				return n * 16 + 8;
			else
				return n * 16 + 9;
		case OR_4:
			if (!isGateActive)
				return n * 16 + 10;
			else
				return n * 16 + 11;
		}

		return 0;
	}

	private int getTextureRow() {
		if (hasPulser())
			return 9;
		else
			return 8;
	}

	@Override
	public String getGuiFile() {
		if (kind == GateKind.Single)
			return DefaultProps.TEXTURE_PATH_GUI + "/gate_interface_1.png";
		else if (kind == GateKind.AND_2 || kind == GateKind.OR_2)
			return DefaultProps.TEXTURE_PATH_GUI + "/gate_interface_2.png";
		else if (kind == GateKind.AND_3 || kind == GateKind.OR_3)
			return DefaultProps.TEXTURE_PATH_GUI + "/gate_interface_3.png";
		else
			return DefaultProps.TEXTURE_PATH_GUI + "/gate_interface_4.png";
	}

}
