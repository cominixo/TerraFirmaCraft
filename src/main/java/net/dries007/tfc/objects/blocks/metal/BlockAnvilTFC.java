/*
 * Work under Copyright. Licensed under the EUPL.
 * See the project README.md and LICENSE.txt for more information.
 */

package net.dries007.tfc.objects.blocks.metal;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import mcp.MethodsReturnNonnullByDefault;
import net.dries007.tfc.TerraFirmaCraft;
import net.dries007.tfc.api.types.Metal;
import net.dries007.tfc.client.TFCGuiHandler;
import net.dries007.tfc.objects.items.metal.ItemAnvil;
import net.dries007.tfc.objects.te.TEAnvilTFC;
import net.dries007.tfc.util.Helpers;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BlockAnvilTFC extends Block implements ITileEntityProvider
{
    public static final PropertyBool AXIS = PropertyBool.create("axis");
    private static final Map<Metal, BlockAnvilTFC> MAP = new HashMap<>();
    private static final AxisAlignedBB AABB_Z = new AxisAlignedBB(0.1875, 0, 0, 0.8125, 0.625, 1);
    private static final AxisAlignedBB AABB_X = new AxisAlignedBB(0, 0, 0.1875, 1, 0.625, 0.8125);

    public static BlockAnvilTFC get(Metal metal)
    {
        return MAP.get(metal);
    }

    public static ItemStack get(Metal metal, int amount)
    {
        return new ItemStack(MAP.get(metal), amount);
    }

    public final Metal metal;

    public BlockAnvilTFC(Metal metal)
    {
        super(Material.IRON);

        this.metal = metal;
        if (MAP.put(metal, this) != null) throw new IllegalStateException("There can only be one.");

        setHardness(4.0F);
        setResistance(10F);
        setHarvestLevel("pickaxe", 0);

        setDefaultState(this.blockState.getBaseState().withProperty(AXIS, false));
    }

    @Override
    @SuppressWarnings("deprecation")
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(AXIS, meta == 0);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(AXIS) ? 0 : 1;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        return state.getValue(AXIS) ? AABB_Z : AABB_X;
    }

    @SideOnly(Side.CLIENT)
    @Override
    @SuppressWarnings("deprecation")
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos)
    {
        return getBoundingBox(state, worldIn, pos).offset(pos);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune)
    {
        return ItemAnvil.get(metal, Metal.ItemType.ANVIL);
    }

    @Override
    public BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, AXIS);
    }

    @Override
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return false;
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player)
    {
        return new ItemStack(ItemAnvil.get(metal, Metal.ItemType.ANVIL));
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        TEAnvilTFC te = Helpers.getTE(worldIn, pos, TEAnvilTFC.class);
        if (te == null)
        {
            return false;
        }
        IItemHandler cap = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if (cap == null)
        {
            return false;
        }
        ItemStack stack = playerIn.getHeldItem(hand);
        for (int i = 0; i < 4; i++)
        {
            if (te.isItemValid(i, stack) && cap.getStackInSlot(i).isEmpty())
            {
                if (!worldIn.isRemote)
                {
                    ItemStack result = cap.insertItem(i, stack, false);
                    playerIn.setHeldItem(hand, result);
                    TerraFirmaCraft.getLog().debug("Inserted {} into slot {}", stack.getDisplayName(), i);
                }
                return true;
            }
        }
        if (!playerIn.isSneaking())
        {
            if (!worldIn.isRemote)
            {
                TFCGuiHandler.openGui(worldIn, pos, playerIn, TFCGuiHandler.Type.ANVIL);
            }
            return true;
        }
        return false;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TEAnvilTFC(metal.getTier());
    }
}
