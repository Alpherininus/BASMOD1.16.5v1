package com.alpherininus.basmod.common.entitys;

import com.alpherininus.basmod.common.entitys.ai.AttackGoal;
import com.alpherininus.basmod.core.init.ItemInit;
import com.alpherininus.basmod.core.util.BasmodConfig;
import com.alpherininus.basmod.core.util.BasmodFonts;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import java.util.UUID;
import java.util.function.Predicate;

public class NPCEntity extends CreatureEntity {

    private static final DataParameter<Integer> NPC_TYPE = EntityDataManager.createKey(NPCEntity.class, DataSerializers.VARINT);

    private static final Predicate<LivingEntity> field_213627_bA = (p_213626_0_) -> p_213626_0_ instanceof MobEntity;

    private static final ResourceLocation KILLER_GUENTER = new ResourceLocation("killer_guenter");
    private static final ResourceLocation NORMAL_GUENTER = new ResourceLocation("normal_guenter");


    public NPCEntity(EntityType<? extends CreatureEntity> p_i50247_1_, World p_i50247_2_) {
        super(p_i50247_1_, p_i50247_2_);
    }

    public static AttributeModifierMap.MutableAttribute registerNPCAttributes() {
        return CreatureEntity.func_233666_p_()
                .createMutableAttribute(Attributes.MOVEMENT_SPEED, 0.25D)
                .createMutableAttribute(Attributes.MAX_HEALTH, 20.0D)
                .createMutableAttribute(Attributes.FOLLOW_RANGE, 30.0D);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected void registerGoals() {
        this.goalSelector.addGoal(1, new SwimGoal(this));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomWalkingGoal(this, 0.6D));
        this.goalSelector.addGoal(11, new LookAtGoal(this, PlayerEntity.class, 10.0F));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected void addPlayerAttackGoals() {
        this.goalSelector.addGoal(1, new AttackGoal(this, 1.0D, true));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, 10, true, false, field_213627_bA));

    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected void registerData() {
        super.registerData();
        this.dataManager.register(NPC_TYPE, 0);

    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void updateAITasks() {

    }

    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        compound.putInt("NPCType", this.getNPCEntityType());

    }

    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);
        this.setNPCType(compound.getInt("NPCType"));

    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected SoundEvent getAmbientSound() {
        if (this.getNPCEntityType() == 69) {
            this.playSound(SoundEvents.ENTITY_VILLAGER_YES, 50.0F, (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
        } else {
            return SoundEvents.ENTITY_RABBIT_AMBIENT;
        }
        return null;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        if (this.getNPCEntityType() == 69) {
            this.playSound(SoundEvents.ENTITY_VILLAGER_HURT, 50.0F, (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
        } else {
            return SoundEvents.ENTITY_RABBIT_HURT;
        }
        return null;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_RABBIT_DEATH;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean attackEntityAsMob(Entity entityIn) {
        if (this.getNPCEntityType() == 99) {
            this.playSound(SoundEvents.ENTITY_RABBIT_ATTACK, 1.0F, (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
            return entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), 8.0F);
        } else {
            return entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), 3.0F);
        }
    }

    public boolean attackEntityFrom(DamageSource source, float amount) {
        return !this.isInvulnerableTo(source) && super.attackEntityFrom(source, amount);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public int getNPCEntityType() {
        return this.dataManager.get(NPC_TYPE);
    }

    public void setNPCType(int npcTypeId) {
        if (npcTypeId == 99) {
            this.getAttribute(Attributes.ARMOR).setBaseValue(12.0D);
            this.goalSelector.addGoal(4, new NPCEntity.EvilAttackGoal(this));
            this.targetSelector.addGoal(1, (new HurtByTargetGoal(this)).setCallsForHelp());
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true));
            if (!this.hasCustomName()) {
                this.setCustomName(new StringTextComponent("Guenter"));
            }
        }

        if (npcTypeId == 42) {
            this.getAttribute(Attributes.ARMOR).setBaseValue(15.0D);
            if (!this.hasCustomName()) {
                String NPC_NAME = "Asuka";
                this.setCustomName(new StringTextComponent(NPC_NAME));
            }
        }

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

        if (npcTypeId == 1) {
            this.getAttribute(Attributes.ARMOR).setBaseValue(15.0D);
            if (!this.hasCustomName()) {
                String NPC_NAME = BasmodConfig.config_npc_name_ID1.get();
                this.setCustomName(new StringTextComponent(NPC_NAME));
            }
        }

        if (npcTypeId == 2) {
            this.getAttribute(Attributes.ARMOR).setBaseValue(15.0D);
            if (!this.hasCustomName()) {
                String NPC_NAME = BasmodConfig.config_npc_name_ID2.get();
                this.setCustomName(new StringTextComponent(NPC_NAME));
            }
        }

        if (npcTypeId == 3) {
            this.getAttribute(Attributes.ARMOR).setBaseValue(15.0D);
            if (!this.hasCustomName()) {
                String NPC_NAME = BasmodConfig.config_npc_name_ID3.get();
                this.setCustomName(new StringTextComponent(NPC_NAME));
            }
        }

        if (npcTypeId == 4) {
            this.getAttribute(Attributes.ARMOR).setBaseValue(15.0D);
            if (!this.hasCustomName()) {
                String NPC_NAME = BasmodConfig.config_npc_name_ID4.get();
                this.setCustomName(new StringTextComponent(NPC_NAME));
            }
        }

        if (npcTypeId == 5) {
            this.getAttribute(Attributes.ARMOR).setBaseValue(15.0D);
            if (!this.hasCustomName()) {
                String NPC_NAME = BasmodConfig.config_npc_name_ID5.get();
                this.setCustomName(new StringTextComponent(NPC_NAME));
            }
        }

        if (npcTypeId == 6) {
            this.getAttribute(Attributes.ARMOR).setBaseValue(15.0D);
            if (!this.hasCustomName()) {
                String NPC_NAME = BasmodConfig.config_npc_name_ID6.get();
                this.setCustomName(new StringTextComponent(NPC_NAME));
            }
        }

        if (npcTypeId == 7) {
            this.getAttribute(Attributes.ARMOR).setBaseValue(15.0D);
            if (!this.hasCustomName()) {
                String NPC_NAME = BasmodConfig.config_npc_name_ID7.get();
                this.setCustomName(new StringTextComponent(NPC_NAME));
            }
        }

        if (npcTypeId == 8) {
            this.getAttribute(Attributes.ARMOR).setBaseValue(15.0D);
            if (!this.hasCustomName()) {
                String NPC_NAME = BasmodConfig.config_npc_name_ID8.get();
                this.setCustomName(new StringTextComponent(NPC_NAME));
            }
        }

        if (npcTypeId == 9) {
            this.getAttribute(Attributes.ARMOR).setBaseValue(15.0D);
            if (!this.hasCustomName()) {
                String NPC_NAME = BasmodConfig.config_npc_name_ID9.get();
                this.setCustomName(new StringTextComponent(NPC_NAME));
            }
        }

        if (npcTypeId == 10) {
            this.getAttribute(Attributes.ARMOR).setBaseValue(15.0D);
            if (!this.hasCustomName()) {
                String NPC_NAME = BasmodConfig.config_npc_name_ID10.get();
                this.setCustomName(new StringTextComponent(NPC_NAME));
            }
        }

        if (npcTypeId == 11) {
            this.getAttribute(Attributes.ARMOR).setBaseValue(15.0D);
            if (!this.hasCustomName()) {
                String NPC_NAME = BasmodConfig.config_npc_name_ID11.get();
                this.setCustomName(new StringTextComponent(NPC_NAME));
            }
        }

        if (npcTypeId == 12) {
            this.getAttribute(Attributes.ARMOR).setBaseValue(15.0D);
            if (!this.hasCustomName()) {
                String NPC_NAME = BasmodConfig.config_npc_name_ID12.get();
                this.setCustomName(new StringTextComponent(NPC_NAME));
            }
        }

        if (npcTypeId == 13) {
            this.getAttribute(Attributes.ARMOR).setBaseValue(15.0D);
            if (!this.hasCustomName()) {
                String NPC_NAME = BasmodConfig.config_npc_name_ID13.get();
                this.setCustomName(new StringTextComponent(NPC_NAME));
            }
        }

        if (npcTypeId == 14) {
            this.getAttribute(Attributes.ARMOR).setBaseValue(15.0D);
            if (!this.hasCustomName()) {
                String NPC_NAME = BasmodConfig.config_npc_name_ID14.get();
                this.setCustomName(new StringTextComponent(NPC_NAME));
            }
        }

        if (npcTypeId == 15) {
            this.getAttribute(Attributes.ARMOR).setBaseValue(15.0D);
            if (!this.hasCustomName()) {
                String NPC_NAME = BasmodConfig.config_npc_name_ID15.get();
                this.setCustomName(new StringTextComponent(NPC_NAME));
            }
        }

        if (npcTypeId == 16) {
            this.getAttribute(Attributes.ARMOR).setBaseValue(15.0D);
            if (!this.hasCustomName()) {
                String NPC_NAME = BasmodConfig.config_npc_name_ID16.get();
                this.setCustomName(new StringTextComponent(NPC_NAME));
            }
        }

        if (npcTypeId == 17) {
            this.getAttribute(Attributes.ARMOR).setBaseValue(15.0D);
            if (!this.hasCustomName()) {
                String NPC_NAME = BasmodConfig.config_npc_name_ID17.get();
                this.setCustomName(new StringTextComponent(NPC_NAME));
            }
        }

        if (npcTypeId == 18) {
            this.getAttribute(Attributes.ARMOR).setBaseValue(15.0D);
            if (!this.hasCustomName()) {
                String NPC_NAME = BasmodConfig.config_npc_name_ID18.get();
                this.setCustomName(new StringTextComponent(NPC_NAME));
            }
        }

        if (npcTypeId == 19) {
            this.getAttribute(Attributes.ARMOR).setBaseValue(15.0D);
            if (!this.hasCustomName()) {
                String NPC_NAME = BasmodConfig.config_npc_name_ID19.get();
                this.setCustomName(new StringTextComponent(NPC_NAME));
            }
        }

        if (npcTypeId == 20) {
            this.getAttribute(Attributes.ARMOR).setBaseValue(15.0D);
            if (!this.hasCustomName()) {
                String NPC_NAME = BasmodConfig.config_npc_name_ID20.get();
                this.setCustomName(new StringTextComponent(NPC_NAME));
            }
        }

        if (npcTypeId == 21) {
            this.getAttribute(Attributes.ARMOR).setBaseValue(15.0D);
            if (!this.hasCustomName()) {
                String NPC_NAME = BasmodConfig.config_npc_name_ID21.get();
                this.setCustomName(new StringTextComponent(NPC_NAME));
            }
        }

        if (npcTypeId == 22) {
            this.getAttribute(Attributes.ARMOR).setBaseValue(15.0D);
            if (!this.hasCustomName()) {
                String NPC_NAME = BasmodConfig.config_npc_name_ID22.get();
                this.setCustomName(new StringTextComponent(NPC_NAME));
            }
        }

        if (npcTypeId == 23) {
            this.getAttribute(Attributes.ARMOR).setBaseValue(15.0D);
            if (!this.hasCustomName()) {
                String NPC_NAME = BasmodConfig.config_npc_name_ID23.get();
                this.setCustomName(new StringTextComponent(NPC_NAME));
            }
        }

        if (npcTypeId == 24) {
            this.getAttribute(Attributes.ARMOR).setBaseValue(15.0D);
            if (!this.hasCustomName()) {
                String NPC_NAME = BasmodConfig.config_npc_name_ID24.get();
                this.setCustomName(new StringTextComponent(NPC_NAME));
            }
        }

        if (npcTypeId == 25) {
            this.getAttribute(Attributes.ARMOR).setBaseValue(15.0D);
            if (!this.hasCustomName()) {
                String NPC_NAME = BasmodConfig.config_npc_name_ID25.get();
                this.setCustomName(new StringTextComponent(NPC_NAME));
            }
        }

        if (npcTypeId == 26) {
            this.getAttribute(Attributes.ARMOR).setBaseValue(15.0D);
            if (!this.hasCustomName()) {
                String NPC_NAME = BasmodConfig.config_npc_name_ID26.get();
                this.setCustomName(new StringTextComponent(NPC_NAME));
            }
        }

        if (npcTypeId == 27) {
            this.getAttribute(Attributes.ARMOR).setBaseValue(15.0D);
            if (!this.hasCustomName()) {
                String NPC_NAME = BasmodConfig.config_npc_name_ID27.get();
                this.setCustomName(new StringTextComponent(NPC_NAME));
            }
        }

        if (npcTypeId == 28) {
            this.getAttribute(Attributes.ARMOR).setBaseValue(15.0D);
            if (!this.hasCustomName()) {
                String NPC_NAME = BasmodConfig.config_npc_name_ID28.get();
                this.setCustomName(new StringTextComponent(NPC_NAME));
            }
        }

        if (npcTypeId == 29) {
            this.getAttribute(Attributes.ARMOR).setBaseValue(15.0D);
            if (!this.hasCustomName()) {
                String NPC_NAME = BasmodConfig.config_npc_name_ID29.get();
                this.setCustomName(new StringTextComponent(NPC_NAME));
            }
        }



        this.dataManager.set(NPC_TYPE, npcTypeId);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean hitByEntity(Entity entityIn) {
        return super.hitByEntity(entityIn);
    }

    @Override
    protected ActionResultType getEntityInteractionResult(PlayerEntity playerIn, Hand hand) {

        if (!world.isRemote()) {

            if (this.getNPCEntityType() == 1) {

                if (playerIn.experienceLevel == 2) {
                    playerIn.sendStatusMessage(new StringTextComponent("Hallo"), true);

                } else if (playerIn.experienceLevel == 4) {
                    playerIn.sendStatusMessage(new StringTextComponent("lul"), true);

                }
                playerIn.sendMessage(new StringTextComponent("Hallo"), UUID.randomUUID());

            }

            if (this.getNPCEntityType() == 2) {
                String character = "\uEA01";

                if (playerIn.experienceLevel == 1) {
                    playerIn.addItemStackToInventory(new ItemStack(ItemInit.BASMOD_BOOK_BLOCKS.get(), 1));

                    playerIn.sendStatusMessage(ITextComponent.getTextComponentOrEmpty("Hier ist die nexte Quest" + "  " + character), true);

                }

                playerIn.sendStatusMessage(ITextComponent.getTextComponentOrEmpty("Hallo :) ." + "  " + character), true);

            }


            return ActionResultType.SUCCESS;
        }

        return super.getEntityInteractionResult(playerIn, hand);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static class EvilAttackGoal extends MeleeAttackGoal {
        public EvilAttackGoal(NPCEntity rabbit) {
            super(rabbit, 1.4D, true);
        }

        protected double getAttackReachSqr(LivingEntity attackTarget) {
            return (double)(4.0F + attackTarget.getWidth());
        }
    }

    public static class NPCData extends AgeableEntity.AgeableData {
        public final int typeData;

        public NPCData(int type) {
            super(1.0F);
            this.typeData = type;
        }
    }

}
