package mffs;

import mffs.gui.GuiCoercionDeriver;
import mffs.gui.GuiForceFieldProjector;
import mffs.gui.GuiFortronCapacitor;
import mffs.render.FXBeam;
import mffs.render.RenderBlockHandler;
import mffs.render.RenderForceFieldProjector;
import mffs.render.RenderFortronCapacitor;
import mffs.tileentity.TileEntityCoercionDeriver;
import mffs.tileentity.TileEntityForceFieldProjector;
import mffs.tileentity.TileEntityFortronCapacitor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import universalelectricity.core.vector.Vector3;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy
{
	@Override
	public void preInit()
	{
		super.preInit();
		MinecraftForge.EVENT_BUS.register(SoundHandler.INSTANCE);
	}

	@Override
	public void init()
	{
		super.init();
		RenderingRegistry.registerBlockHandler(new RenderBlockHandler());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFortronCapacitor.class, new RenderFortronCapacitor());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityForceFieldProjector.class, new RenderForceFieldProjector());
	}

	@Override
	public World getClientWorld()
	{
		return FMLClientHandler.instance().getClient().theWorld;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity != null)
		{
			if (tileEntity.getClass() == TileEntityFortronCapacitor.class)
			{
				return new GuiFortronCapacitor(player, (TileEntityFortronCapacitor) tileEntity);
			}
			else if (tileEntity.getClass() == TileEntityForceFieldProjector.class)
			{
				return new GuiForceFieldProjector(player, (TileEntityForceFieldProjector) tileEntity);
			}
			else if (tileEntity.getClass() == TileEntityCoercionDeriver.class)
			{
				return new GuiCoercionDeriver(player, (TileEntityCoercionDeriver) tileEntity);
			}
		}

		return null;
	}

	@Override
	public void renderBeam(World world, Vector3 position, Vector3 target, float red, float green, float blue, int age)
	{
		FMLClientHandler.instance().getClient().effectRenderer.addEffect(new FXBeam(world, position, target, red, green, blue, age));
	}
}