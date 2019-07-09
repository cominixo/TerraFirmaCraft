package net.dries007.tfc.api.recipes;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

import net.dries007.tfc.api.registries.TFCRegistries;
import net.dries007.tfc.objects.inventory.ingredient.IIngredient;

public class QuernRecipe extends IForgeRegistryEntry.Impl<QuernRecipe>
{
    @Nullable
    public static QuernRecipe get(ItemStack item)
    {
        return TFCRegistries.QUERN.getValuesCollection().stream().filter(x -> x.isValidInput(item)).findFirst().orElse(null);
    }

    private IIngredient<ItemStack> inputItem;
    private ItemStack outputItem;

    public QuernRecipe(ResourceLocation name, IIngredient<ItemStack> input, ItemStack output)
    {
        this.inputItem = input;
        this.outputItem = output;

        if (inputItem == null || outputItem == null)
        {
            throw new IllegalArgumentException("Input and output are not allowed to be empty");
        }
        setRegistryName(name);
    }

    public ItemStack getOutputItem()
    {
        return outputItem;
    }

    private boolean isValidInput(ItemStack inputItem)
    {
        return this.inputItem.testIgnoreCount(inputItem);
    }
}
