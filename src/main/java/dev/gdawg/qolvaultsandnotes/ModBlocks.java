package dev.gdawg.qolvaultsandnotes;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    // Create a Deferred Register to hold Blocks which will all be registered under the "examplemod" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(QOLVaultsAndNotes.MODID);

    // Creates a new Block with the id "examplemod:example_block", combining the namespace and path
    public static final DeferredBlock<CreateBlock> BULLETIN_BOARD_BLOCK = BLOCKS.register("bulletin_board",
            id -> new CreateBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).noOcclusion().randomTicks().setId(ResourceKey.create(Registries.BLOCK, id))));

    // .setId just has to do with setting a block id for a block, since apparently since of 1.21 and further forward, they added so you have to manually add that stuff
    // ExampleBlock is a new class or whatever made because registerSimpleBlock is not that functionally useful, instead it was just better to remake the whole thing into it's own register
    // ExampleBlock was also made in order to give the correct hitbox to the block when placed
    // MapColor.whatever means what colour/block the block is when placed in the world and viewed on a map, pretty much no use
    // .noOcclusion removes that weird seeing through the world issue you get when you make the model for a block smaller than the default size for a block
    // BlockBehaviour and such creates a blank thinga majig that you then add stuff into, like mapcolor, no occlusion, setid, and whatever else you might possibly add. Configuring the block manually pretty much
    // "useful shortcuts" ctrl + alt + o removes all unecessary imports, ones that aren't being used
    // alt + enter when clicked on an error thing, if it is missing an import, it will say something about a missing class, then it will automatically put the import in the file
    // config.java was completely removed since it only served a purpose with logging and stuff, same with code that had to do with logging got removed cus it isn't needed
    // that's why I think examplemodclient is so small, but Idk
    // when adding a new block, you'd just do everything the same as this block section these comments are close to, as well as resources/assets/examplemod blockstates, models, and textures, will be "added the same"
    // next issue to tackle with this code is the issue with the texture/model not showing in your hand or inventory
    // another issue to tackle is that the block is always placed facing north
    // last issue to tackle is to rename everything that says example whatever to what it actually is
    // you do this by either shift + f6 which I didn't feel like it worked
    // or you use ctrl + r to search and replace, which I don't know if it works either

    // Register the Deferred Register to the mod event bus so blocks get registered
    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}