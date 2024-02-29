package net.brdle.collectorsreap.common.block;

import net.brdle.collectorsreap.Util;
import net.brdle.collectorsreap.common.item.CRItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeHooks;
import org.jetbrains.annotations.NotNull;

public class SmallLimeBushBlock extends BushBlock implements BonemealableBlock {
	public static final int MAX_AGE = 1;
	public static final IntegerProperty AGE = BlockStateProperties.AGE_1;
	public static final VoxelShape SMALL_SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 11.0, 12.0);
	private static final VoxelShape MEDIUM_SHAPE = Shapes.or(Block.box(0.0D, 8.0D, 0.0D, 16.0D, 16.0D, 16.0D), Block.box(6.0D, 0.0D, 6.0D, 10.0D, 8.0D, 10.0D));

	public SmallLimeBushBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
	}

	@SuppressWarnings("deprecation")
	@Override
	public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos, @NotNull CollisionContext pContext) {
		return state.getValue(AGE) == MAX_AGE ? MEDIUM_SHAPE : SMALL_SHAPE;
	}

	@SuppressWarnings("deprecation")
	@Override
	public @NotNull VoxelShape getCollisionShape(@NotNull BlockState pState, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos, @NotNull CollisionContext pContext) {
		return getShape(pState, pLevel, pPos, pContext);
	}

	@SuppressWarnings("deprecation")
	@Override
	public @NotNull ItemStack getCloneItemStack(@NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull BlockState state) {
		return Util.gs(CRItems.LIME_SEEDS);
	}

	@Override
	protected boolean mayPlaceOn(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
		return state.is(BlockTags.DIRT);
	}

	@Override
	public boolean canSurvive(@NotNull BlockState state, @NotNull LevelReader level, BlockPos pos) {
		BlockPos below = pos.below();
		return this.mayPlaceOn(level.getBlockState(below), level, below);
	}

	@Override
	public boolean isRandomlyTicking(@NotNull BlockState state) {
		return true;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void randomTick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, RandomSource random) {
		if (ForgeHooks.onCropsGrowPre(level, pos, state, random.nextInt(20) == 0)) {
			this.performBonemeal(level, random, pos, state);
			ForgeHooks.onCropsGrowPost(level, pos, state);
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
		pBuilder.add(AGE);
	}

	@Override
	public boolean isValidBonemealTarget(@NotNull LevelReader pLevel, @NotNull BlockPos pPos, @NotNull BlockState pState, boolean pIsClient) {
		return false;
	}

	@Override
	public boolean isBonemealSuccess(@NotNull Level world, @NotNull RandomSource rand, @NotNull BlockPos pos, @NotNull BlockState state) {
		return true;
	}

	@Override
	public void performBonemeal(@NotNull ServerLevel world, @NotNull RandomSource pRandom, @NotNull BlockPos pPos, BlockState pState) {
		int i = pState.getValue(AGE);
		if (i < MAX_AGE) {
			BlockState newState = pState.setValue(AGE, Math.min(MAX_AGE, pState.getValue(AGE) + 1));
			world.setBlock(pPos, newState, 2);
			world.gameEvent(GameEvent.BLOCK_CHANGE, pPos, GameEvent.Context.of(newState));
		} else if (world.isEmptyBlock(pPos.above())) {
			world.setBlockAndUpdate(pPos, CRBlocks.LIME_BUSH.get().defaultBlockState());
			world.setBlockAndUpdate(pPos.above(), CRBlocks.LIME_BUSH.get().defaultBlockState().setValue(LimeBushBlock.HALF, DoubleBlockHalf.UPPER));
		}
	}


	@Override
	public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return 60;
	}

	@Override
	public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return 30;
	}
}
