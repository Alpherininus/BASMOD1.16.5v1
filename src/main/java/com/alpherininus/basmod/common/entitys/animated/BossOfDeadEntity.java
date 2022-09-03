package com.alpherininus.basmod.common.entitys.animated;

import com.alpherininus.basmod.Basmod;
import com.alpherininus.basmod.core.init.ItemInit;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ShootableItem;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.play.server.SChangeGameStatePacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.BossInfo;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerBossInfo;
import net.minecraft.world.server.ServerWorld;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.network.ISyncable;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class BossOfDeadEntity extends MonsterEntity implements IAnimatable, ISyncable {

    private static final EntityPredicate PLAYER_INVADER_CONDITION = (new EntityPredicate()).setDistance(64.0D);
    private static final Predicate<LivingEntity> NOT_UNDEAD = (p_213797_0_) -> p_213797_0_.getCreatureAttribute() != CreatureAttribute.UNDEAD && p_213797_0_.attackable();
    private final ServerBossInfo bossInfo = (ServerBossInfo)(new ServerBossInfo(new StringTextComponent("-= Dance of Death =-"), BossInfo.Color.RED, BossInfo.Overlay.PROGRESS)).setPlayEndBossMusic(true).setDarkenSky(true).setCreateFog(true);
    private final int id = 1;
    private final int state = 0;
    private boolean slowed;

    private final Minecraft mc = Minecraft.getInstance();


    private <E extends IAnimatable>PlayState predicate(AnimationEvent<E> event) {
        event.getController().setAnimation(new AnimationBuilder().addAnimation("idle", true));
        return PlayState.CONTINUE;
    }

    private AnimationFactory factory = new AnimationFactory(this);

    public BossOfDeadEntity(EntityType<? extends MonsterEntity> entityType, World worldIn) {
        super(entityType, worldIn);

    }

    protected void registerGoals() {
        this.addSwimGoals();
        this.goalSelector.addGoal(3, new LookAtGoal(this, LivingEntity.class, 16.0F));

        this.goalSelector.addGoal(2, new LookAtGoal(this, PlayerEntity.class, 32.0F));
        this.goalSelector.addGoal(2, new TemptGoal(this, 1.0D, Ingredient.fromItems(ItemInit.TOTEM_OFF_DESPAWN.get()), true));

        this.goalSelector.addGoal(1, new LookRandomlyGoal(this));
        this.addTargetGoals();
    }

    protected void addSwimGoals() {
        this.goalSelector.addGoal(0, new SwimGoal(this));
        this.goalSelector.addGoal(1, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
    }

    protected void addTargetGoals() {
        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this)).setCallsForHelp(BasBossEntity.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true));
        this.targetSelector.addGoal(2, new MoveTowardsTargetGoal(this, 1.0D, 32.0F));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AnimalEntity.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, CreatureEntity.class, true));

    }

    public static AttributeModifierMap setCustomBossOfDeadAttributes() {
        return MobEntity.func_233666_p_()
                .createMutableAttribute(Attributes.MAX_HEALTH, 1500.0D)
                .createMutableAttribute(Attributes.ATTACK_DAMAGE, 5.0f)
                .createMutableAttribute(Attributes.ATTACK_SPEED, 2.0f)
                .createMutableAttribute(Attributes.MOVEMENT_SPEED, 0.25D)
                .createMutableAttribute(Attributes.FOLLOW_RANGE, 64.0D)
                .createMutableAttribute(Attributes.ATTACK_KNOCKBACK, 1.0f).create();

    }

    @Override
    public ItemStack findAmmo(ItemStack shootable) {
        if (shootable.getItem() instanceof ShootableItem) {
            Predicate<ItemStack> predicate = ((ShootableItem)shootable.getItem()).getAmmoPredicate();
            ItemStack itemstack = ShootableItem.getHeldAmmo(this, predicate);
            return itemstack.isEmpty() ? new ItemStack(Items.ARROW) : itemstack;
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    protected int getExperiencePoints(PlayerEntity player) {
        return 5 + this.world.rand.nextInt(11);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private <E extends IAnimatable>PlayState predicateAttack(AnimationEvent<E> event) {

        if (this.getAttackTarget() != null) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("attack", false));
            return PlayState.CONTINUE;

        }

        return PlayState.STOP;
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(
                new AnimationController(this, "BossController", 0, this::predicate));

        animationData.addAnimationController(
                new AnimationController(this, "attackController", 0, this::predicateAttack));


    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public void setAttackingPlayer(@Nullable PlayerEntity p_230246_1_) {
        super.setAttackingPlayer(p_230246_1_);
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockIn) {
        this.playSound(SoundEvents.BLOCK_METAL_STEP, 0.50F, 1.0F);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isInWater()) {
            return SoundEvents.ENTITY_WITCH_HURT;
        } else {
            if (this.isBurning()) {
                return SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT;
            } else {
                return SoundEvents.ENTITY_WITCH_AMBIENT;

            }
        }
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.ENTITY_WITCH_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_WITHER_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 10.5F;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void dropSpecialItems(DamageSource source, int looting, boolean recentlyHitIn) {
        super.dropSpecialItems(source, looting, recentlyHitIn);
        Entity entity = source.getTrueSource();
        ItemEntity itementity = this.entityDropItem(Items.DIAMOND_HOE);
        if (itementity != null) {
            itementity.setNoDespawn();
        }
        if (entity instanceof CreeperEntity) {
            CreeperEntity creeperentity = (CreeperEntity)entity;
            if (creeperentity.ableToCauseSkullDrop()) {
                ItemStack itemstack = this.getSkullDrop();
                if (!itemstack.isEmpty()) {
                    creeperentity.incrementDroppedSkulls();
                    this.entityDropItem(itemstack);
                }
            }
        }
    }

    protected ItemStack getSkullDrop() {
        return new ItemStack(Items.ZOMBIE_HEAD);
    }

    public CreatureAttribute getCreatureAttribute() {
        return CreatureAttribute.UNDEAD;
    }

    public boolean onLivingFall(float distance, float damageMultiplier) {
        return false;
    }

    @Override
    public boolean addPotionEffect(EffectInstance effectInstanceIn) {
        return false;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void updateAITasks() {

        if (this.ticksExisted % 20 == 0) {
            this.heal(1.5F);
        }

        if (this.getHealth() <= 10.5F) {
            this.heal(500.5F);
        }

        if (this.world.isThundering()) {
            this.setAggroed(true);
            if (this.isAggressive()) {
                if (this.ticksExisted % 5 == 0) {
                    this.heal(50.5F);
                }
            }
            if ((this.ticksExisted + this.getEntityId()) % 1200 == 0) {
                Effect effect1w = Effects.WITHER;
                List<ServerPlayerEntity> list = ((ServerWorld) this.world).getPlayers((p_210138_1_) -> this.getDistanceSq(p_210138_1_) < 2500.0D && p_210138_1_.interactionManager.survivalOrAdventure());
                int j = 2;
                int k = 6000;
                int l = 1200;

                for (ServerPlayerEntity serverplayerentity : list) {
                    if (!serverplayerentity.isPotionActive(effect1w) || serverplayerentity.getActivePotionEffect(effect1w).getAmplifier() < 1 || serverplayerentity.getActivePotionEffect(effect1w).getDuration() < 1200) {
                        serverplayerentity.connection.sendPacket(new SChangeGameStatePacket(SChangeGameStatePacket.HIT_PLAYER_ARROW, this.isSilent() ? 0.0F : 1.0F));
                        serverplayerentity.addPotionEffect(new EffectInstance(effect1w, 3000, 1));
                    }

                }
            }
        }

        int i = 1200;
        if ((this.ticksExisted + this.getEntityId()) % 1200 == 0) {
            Effect effect1 = Effects.WEAKNESS;
            Effect effect2 = Effects.UNLUCK;
            Effect effect3 = Effects.MINING_FATIGUE;

            List<ServerPlayerEntity> list = ((ServerWorld)this.world).getPlayers((p_210138_1_) -> this.getDistanceSq(p_210138_1_) < 2500.0D && p_210138_1_.interactionManager.survivalOrAdventure());
            int j = 2;
            int k = 6000;
            int l = 1200;

            for(ServerPlayerEntity serverplayerentity : list) {
                if (!serverplayerentity.isPotionActive(effect1) || serverplayerentity.getActivePotionEffect(effect1).getAmplifier() < 2 || serverplayerentity.getActivePotionEffect(effect1).getDuration() < 1200) {
                    serverplayerentity.connection.sendPacket(new SChangeGameStatePacket(SChangeGameStatePacket.HIT_PLAYER_ARROW, this.isSilent() ? 0.0F : 1.0F));
                    serverplayerentity.addPotionEffect(new EffectInstance(effect1, 6000, 2));
                }

                if (this.hurtTime % 10 == 0) {
                    if (!serverplayerentity.isPotionActive(effect2) || serverplayerentity.getActivePotionEffect(effect2).getAmplifier() < 2 || serverplayerentity.getActivePotionEffect(effect2).getDuration() < 1200) {
                        serverplayerentity.connection.sendPacket(new SChangeGameStatePacket(SChangeGameStatePacket.HIT_PLAYER_ARROW, this.isSilent() ? 0.0F : 1.0F));
                        serverplayerentity.addPotionEffect(new EffectInstance(effect2, 6000, 2));
                    }

                }

                if (this.deathTime % 30 == 0) {
                    if (!serverplayerentity.isPotionActive(effect3) || serverplayerentity.getActivePotionEffect(effect3).getAmplifier() < 2 || serverplayerentity.getActivePotionEffect(effect3).getDuration() < 1200) {
                        serverplayerentity.connection.sendPacket(new SChangeGameStatePacket(SChangeGameStatePacket.HIT_PLAYER_ARROW, this.isSilent() ? 0.0F : 1.0F));
                        serverplayerentity.addPotionEffect(new EffectInstance(effect3, 6000, 2));
                    }

                }

            }
        }

        this.bossInfo.setPercent(this.getHealth() / this.getMaxHealth());

    }

    public void addTrackingPlayer(ServerPlayerEntity player) {
        super.addTrackingPlayer(player);
        this.bossInfo.addPlayer(player);
    }

    public void removeTrackingPlayer(ServerPlayerEntity player) {
        super.removeTrackingPlayer(player);
        this.bossInfo.removePlayer(player);
    }

    public ActionResultType getEntityInteractionResult(PlayerEntity playerIn, Hand hand) {

        Minecraft mc = Minecraft.getInstance();
        ItemStack itemstack = playerIn.getHeldItem(hand);

        if (!world.isRemote()) {
            assert mc.world != null;
            if (this.getHealth() <= 10.5F)
                mc.player.sendMessage(new TranslationTextComponent("entity." + Basmod.MOD_ID + ".boss_of_dead" + ".talking" + ".last_talk"), UUID.randomUUID());

            playerIn.addStat(Stats.TALKED_TO_VILLAGER);

        }

        return ActionResultType.func_233537_a_(world.isRemote);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean canBeHitWithPotion() {
        return false;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onAnimationSync(int id, int state) {

    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void checkDespawn() {
        assert mc.player != null;

        if (this.world.getDifficulty() == Difficulty.PEACEFUL && this.isDespawnPeaceful()) {
            this.remove();
        } else {
            this.idleTime = 0;
        }

        ItemStack offhand = mc.player.getItemStackFromSlot(EquipmentSlotType.OFFHAND);
        if (offhand.getItem() == ItemInit.TOTEM_OFF_DESPAWN.get()) {
            this.remove();
        } else {
            this.idleTime = 5;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public void livingTick() {
        super.livingTick();

        if (this.world.isRemote) {

            for(int i = 0; i < 2; ++i) {
                this.world.addParticle(ParticleTypes.FALLING_LAVA, this.getPosXRandom(0.15D), this.getPosYRandom(), this.getPosZRandom(0.15D), 0.0D, 0.0D, 0.0D);
            }
        }

    }

}
