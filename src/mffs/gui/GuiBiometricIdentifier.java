package mffs.gui;

import mffs.ModularForceFieldSystem;
import mffs.api.card.ICardIdentification;
import mffs.api.security.Permission;
import mffs.base.GuiBase;
import mffs.base.TileEntityBase.TilePacketType;
import mffs.container.ContainerBiometricIdentifier;
import mffs.gui.button.GuiButtonPress;
import mffs.tileentity.TileEntityBiometricIdentifier;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import universalelectricity.core.vector.Vector2;
import universalelectricity.prefab.network.PacketManager;
import cpw.mods.fml.common.network.PacketDispatcher;

public class GuiBiometricIdentifier extends GuiBase
{
	private TileEntityBiometricIdentifier tileEntity;

	public GuiBiometricIdentifier(EntityPlayer player, TileEntityBiometricIdentifier tileEntity)
	{
		super(new ContainerBiometricIdentifier(player, tileEntity), tileEntity);
		this.tileEntity = tileEntity;
	}

	@Override
	public void initGui()
	{
		this.textFieldPos = new Vector2(109, 92);
		super.initGui();

		int x = 0;
		int y = 0;

		for (int i = 0; i < Permission.getPermissions().length; i++)
		{
			x++;
			this.buttonList.add(new GuiButtonPress(i + 1, this.width / 2 - 50 + 20 * x, this.height / 2 - 75 + 20 * y, new Vector2(18, 18 * i), this, Permission.getPermissions()[i].name));

			if (i % 3 == 0 && i != 0)
			{
				x = 0;
				y++;
			}
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int x, int y)
	{
		this.fontRenderer.drawString(this.tileEntity.getInvName(), this.xSize / 2 - this.fontRenderer.getStringWidth(this.tileEntity.getInvName()) / 2, 6, 4210752);

		this.drawTextWithTooltip("rights", "%1", 85, 22, x, y, 0);

		try
		{
			if (this.tileEntity.getManipulatingCard() != null)
			{
				ICardIdentification idCard = (ICardIdentification) this.tileEntity.getManipulatingCard().getItem();

				if (idCard.getUsername(this.tileEntity.getManipulatingCard()) != null)
				{
					for (int i = 0; i < this.buttonList.size(); i++)
					{
						if (this.buttonList.get(i) instanceof GuiButtonPress)
						{
							GuiButtonPress button = (GuiButtonPress) this.buttonList.get(i);
							button.drawButton = true;

							int permissionID = i - 1;

							if (Permission.getPermission(permissionID) != null)
							{
								if (idCard.hasPermission(this.tileEntity.getManipulatingCard(), Permission.getPermission(permissionID)))
								{
									button.stuck = true;
								}
								else
								{
									button.stuck = false;
								}
							}
						}
					}
				}
			}
			else
			{
				for (Object button : this.buttonList)
				{
					if (button instanceof GuiButtonPress)
					{
						((GuiButtonPress) button).drawButton = false;
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		this.textFieldFrequency.drawTextBox();

		this.drawTextWithTooltip("master", 28, 90 + (this.fontRenderer.FONT_HEIGHT / 2), x, y);
		super.drawGuiContainerForegroundLayer(x, y);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y)
	{
		super.drawGuiContainerBackgroundLayer(f, x, y);

		this.drawSlot(87, 90);

		this.drawSlot(7, 34);
		this.drawSlot(7, 90);

		// Internal Inventory
		for (int var4 = 0; var4 < 9; var4++)
		{
			this.drawSlot(8 + var4 * 18 - 1, 110);
		}
	}

	@Override
	protected void actionPerformed(GuiButton guiButton)
	{
		super.actionPerformed(guiButton);

		if (guiButton.id > 0)
		{
			PacketDispatcher.sendPacketToServer(PacketManager.getPacket(ModularForceFieldSystem.CHANNEL, this.tileEntity, TilePacketType.TOGGLE_MODE.ordinal(), guiButton.id - 1));
		}
	}
}