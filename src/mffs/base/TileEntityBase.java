package mffs.base;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import mffs.ModularForceFieldSystem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.prefab.implement.IRedstoneReceptor;
import universalelectricity.prefab.implement.IRotatable;
import universalelectricity.prefab.network.IPacketReceiver;
import universalelectricity.prefab.network.PacketManager;
import universalelectricity.prefab.tile.TileEntityDisableable;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public abstract class TileEntityBase extends TileEntityDisableable implements IPacketReceiver, IRotatable, IRedstoneReceptor
{
	public enum TPacketType
	{
		NONE, DESCRIPTION, FREQUENCY, TOGGLE_ACTIVATION, TOGGLE_MODE, INVENTORY;
	}

	/**
	 * Is the machine active and working?
	 */
	private boolean isActive = false;

	/**
	 * Is this machine switched on internally via GUI?
	 */
	private boolean isRedstoneActive = false;

	/**
	 * The players to send packets to for machine update info.
	 */
	public final List<EntityPlayer> playersUsing = new ArrayList<EntityPlayer>();

	/**
	 * Override this for packet updating list.
	 */
	public List getPacketUpdate()
	{
		List objects = new ArrayList();
		objects.add(TPacketType.DESCRIPTION.ordinal());
		objects.add(this.isActive);
		return objects;
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		/**
		 * Packet Update for Client only when GUI is open.
		 */
		if (this.ticks % 4 == 0 && this.playersUsing.size() > 0)
		{
			for (EntityPlayer player : this.playersUsing)
			{
				PacketDispatcher.sendPacketToPlayer(this.getDescriptionPacket(), (Player) player);
			}
		}
	}

	@Override
	public Packet getDescriptionPacket()
	{
		return PacketManager.getPacket(ModularForceFieldSystem.CHANNEL, this, this.getPacketUpdate().toArray());
	}

	@Override
	public void handlePacketData(INetworkManager network, int packetType, Packet250CustomPayload packet, EntityPlayer player, ByteArrayDataInput dataStream)
	{
		try
		{
			this.onReceivePacket(dataStream.readInt(), dataStream);
		}
		catch (Exception e)
		{
			ModularForceFieldSystem.LOGGER.severe(MessageFormat.format("Packet receiving failed: {0}", this.getClass().getSimpleName()));
			e.printStackTrace();
		}
	}

	/**
	 * Inherit this function to receive packets. Make sure this function is supered.
	 * 
	 * @throws IOException
	 */
	public void onReceivePacket(int packetID, ByteArrayDataInput dataStream) throws IOException
	{
		if (packetID == TPacketType.DESCRIPTION.ordinal())
		{
			boolean prevActive = this.isActive;
			this.isActive = dataStream.readBoolean();

			if (prevActive != this.isActive)
			{
				this.worldObj.markBlockForRenderUpdate(this.xCoord, this.yCoord, this.zCoord);
			}
		}
		else if (packetID == TPacketType.TOGGLE_ACTIVATION.ordinal())
		{
			this.toggleActive();
		}
	}

	public boolean isPoweredByRedstone()
	{
		return this.worldObj.isBlockIndirectlyGettingPowered(this.xCoord, this.yCoord, this.zCoord);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound)
	{
		super.readFromNBT(nbttagcompound);
		this.isActive = nbttagcompound.getBoolean("isActive");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound)
	{
		super.writeToNBT(nbttagcompound);

		nbttagcompound.setBoolean("isActive", this.isActive);
	}

	public boolean isActive()
	{
		return this.isActive;
	}

	public void setActive(boolean flag)
	{
		this.isActive = flag;
		this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
	}

	public void toggleActive()
	{
		if (this.isActive())
		{
			this.setActive(false);
		}
		else
		{
			this.setActive(true);
		}
	}

	/**
	 * Direction Methods
	 */
	@Override
	public ForgeDirection getDirection(IBlockAccess world, int x, int y, int z)
	{
		return ForgeDirection.getOrientation(this.getBlockMetadata());
	}

	@Override
	public void setDirection(World world, int x, int y, int z, ForgeDirection facingDirection)
	{
		this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, facingDirection.ordinal(), 3);
	}

	@Override
	public void onPowerOn()
	{
		this.setActive(true);
	}

	@Override
	public void onPowerOff()
	{
		this.setActive(false);
	}
}