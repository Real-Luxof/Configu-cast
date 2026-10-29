package com.luxof.configucast.meth.equationparts;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.iota.BooleanIota;
import at.petrak.hexcasting.api.casting.iota.DoubleIota;
import at.petrak.hexcasting.api.casting.iota.EntityIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.ListIota;
import at.petrak.hexcasting.api.casting.iota.NullIota;
import at.petrak.hexcasting.api.casting.iota.Vec3Iota;

import com.luxof.configucast.meth.MathException;

import static com.luxof.configucast.Configucast.prepareVariables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public final class VariableEP implements EquationPart {

    /** small internal class because <code>Map</code>s are generic at runtime */
    private static record Enchantments(Map<Enchantment, Integer> enchantments) {}

    public String[] variable;
    public VariableEP(String variable) {
        this.variable = variable.substring(1).split(Pattern.quote("."));
    }
    @Override public Object getValue() { return this; }
    @Override public String strRepr() { return "$" + Arrays.toString(variable); }

    public static EquationPart dereferenceVariable(
        VariableEP vep,
        long originalAmount,
        CastingEnvironment env,
        CastingImage img
    ) {
        return dereferenceVariable(vep, originalAmount, env, img, new HashMap<>());
    }

    public static EquationPart dereferenceVariable(
        VariableEP vep,
        long originalAmount,
        CastingEnvironment env,
        CastingImage img,
        Map<String, Object> variables
    ) {
        prepareVariables(originalAmount, env, img, variables);
        return dereferenceVariable(vep, variables);
    }

    public static EquationPart dereferenceVariable(
        VariableEP vep,
        Map<String, Object> variables
    ) {
        // initialData... like, INITIAL-D, THE ARCADE GAMES?!?!?!??!?!?!?!?!
        Object data;
        String first = vep.variable[0];
        if (!variables.containsKey(first))
            throw new MathException(
                "Failed to interpret math equation due to unknown variable: \"" + first + "\""
            );
        data = variables.get(first);

        for (int i = 1; i < vep.variable.length; i++) {
            Object field = vep.variable[i].startsWith("first")
                ? dereferenceVariable(new VariableEP(vep.variable[i]), variables).getValue()
                : vep.variable[i];

            data = deIotaThisIotaIfPossible(data);


            if (data instanceof LivingEntity living) {
                if (!(field instanceof String attribute))
                    throw new MathException("Failed to interpret math equation in variable-accessing: entities have only string attributes.");

                data = switch (attribute) {
                    case "width" -> living.getWidth();
                    case "height" -> living.getHeight();
                    case "pos" -> living.getPos();
                    case "eyepos" -> living.getEyePos();
                    case "lookdir" -> living.getRotationVector();
                    case "age" -> living.age;
                    case "uuid" -> living.getUuidAsString();
                    case "preferredHand" -> living.preferredHand.ordinal();
                    case "maxHealth" -> living.getMaxHealth();
                    case "health" -> living.getHealth();
                    case "armorAttrib" -> living.getArmor();
                    case "equipped" -> getEquipped(living);
                    case "velocity" -> living.getVelocity();
                    case "fallDistance" -> living.fallDistance;
                    case "noClip" -> living.noClip ? 1.0 : 0.0;
                    case "statusEffects" -> List.copyOf(living.getStatusEffects());
                    case "airTicks" -> living.getAir();
                    case "frozenTicks" -> living.getFrozenTicks();
                    case "inPowderSnow" -> living.inPowderSnow;
                    case "isOnFire" -> living.isOnFire();
                    case "isGlowing" -> living.isGlowing();
                    case "isBlocking" -> living.isBlocking();
                    case "isSwimming" -> living.isSwimming();
                    case "isCrawling" -> living.isCrawling();
                    case "isSneaking" -> living.isSneaking();
                    case "isFallFlying" -> living.isFallFlying();
                    case "isFireImmune" -> living.isFireImmune();
                    case "isInLava" -> living.isInLava();
                    case "isInvisible" -> living.isInvisible();
                    case "isOnGround" -> living.isOnGround();
                    case "isOnRail" -> living.isOnRail();
                    case "isSleeping" -> living.isSleeping();
                    case "isSprinting" -> living.isSprinting();
                    case "isTouchingWater" -> living.isTouchingWater();
                    // NOTE: private methods, for some reason
                    //case "isBeingRainedOn" -> living.isBeingRainedOn();
                    //case "isInsideBubbleColumn" -> living.isInsideBubbleColumn();
                    case "isTouchingWaterOrRain" -> living.isTouchingWaterOrRain();
                    case "isInsideWaterOrBubbleColumn" -> living.isInsideWaterOrBubbleColumn();
                    case "isSubmergedInWater" -> living.isSubmergedInWater();
                    case "isSubmergedInLiquid" -> living.isSubmergedInWater() || living.isInLava();
                    case "isWet" -> living.isWet();
                    default -> throw new MathException("Failed to interpret math equation in variable-accessing: entities have no attribute named \"" + attribute + "\".");
                };
            }

            else if (data instanceof Vec3d vec) {
                if (!(field instanceof String attribute))
                    throw new MathException("Failed to interpret math equation in variable-accessing: vectors have only string attributes.");

                data = switch (attribute) {
                    case "x" -> vec.x;
                    case "y" -> vec.y;
                    case "z" -> vec.z;
                    case "normal" -> vec.normalize();
                    case "negate" -> vec.negate();
                    default -> throw new MathException("Failed to interpret math equation in variable-accessing: vectors have no attribute named \"" + attribute + "\".");
                };
            }

            else if (data instanceof List<?> list) {
                if (field instanceof Integer index)
                    data = list.get(index % list.size());

                else if (field instanceof String attribute) {
                    if (attribute.equals("size")) data = list.size();
                    throw new MathException("Failed to interpret math equation in variable-accessing: lists have no attribute named \"" + attribute + "\".");

                } else
                    throw new MathException("Failed to interpret math equation in variable-accessing: lists have only string and integer attributes.");

            } else if (data instanceof ItemStack itemStack) {
                if (!(field instanceof String attribute))
                    throw new MathException("Failed to interpret math equation in variable-accessing: item stacks have only string attributes.");

                data = switch (attribute) {
                    case "item" -> itemStack.getItem().getName().toString();
                    case "count" -> itemStack.getCount();
                    case "damage" -> itemStack.getDamage();
                    case "durability" -> itemStack.getMaxDamage() - itemStack.getDamage();
                    case "maxDurability" -> itemStack.getMaxDamage();
                    case "hasCustomName" -> itemStack.hasCustomName();
                    case "hasEnchantments" -> itemStack.hasEnchantments();
                    case "hasGlint" -> itemStack.hasGlint();
                    case "hasDurability" -> itemStack.isDamageable();
                    case "isDamaged" -> itemStack.isDamaged();
                    case "isEmpty" -> itemStack.isEmpty();
                    case "isEnchantable" -> itemStack.isEnchantable();
                    case "isFood" -> itemStack.isFood();
                    case "isStackable" -> itemStack.isStackable();
                    case "enchantments" -> EnchantmentHelper.get(itemStack);
                    default -> throw new MathException("Failed to interpret math equation in variable-accessing: item stacks have no attribute named \"" + attribute + "\".");
                };
            }

            else if (data instanceof StatusEffectInstance statusEffect) {
                if (!(field instanceof String attribute))
                    throw new MathException("Failed to interpret math equation in variable-accessing: status effects have only string attributes.");

                data = switch (attribute) {
                    case "duration" -> statusEffect.getDuration();
                    case "level" -> statusEffect.getAmplifier();
                    // "is this given by a beacon or conduit?" (semi-transparent particles)
                    case "isAmbient" -> statusEffect.isAmbient();
                    case "showIcon" -> statusEffect.shouldShowIcon();
                    case "showParticles" -> statusEffect.shouldShowParticles();
                    default -> throw new MathException("Failed to interpret math equation in variable-accessing: status effects have no attribute named \"" + attribute + "\".");
                };
            }

            else if (data instanceof Enchantments enchantments) {
                if (!(field instanceof String attribute))
                    throw new MathException("Failed to interpret math equation in variable-accessing: enchantment maps have only string attributes.");

                data = enchantments.enchantments().getOrDefault(
                    Registries.ENCHANTMENT.get(new Identifier(attribute)),
                    0
                );
            }

            else
                throw new MathException("Failed to interpret math equation in variable-accessing: this data type has no attributes to access.");
        }

        data = deIotaThisIotaIfPossible(data);
        data = data instanceof Number num ? new NumberEP((double)num)
            : data instanceof LivingEntity ? new MathException("Failed to interpret math equation in variable-accessing: variable accessing may NOT return entities (no support).")
            : data instanceof Vec3d vec ? new VecEP(vec)
            : data instanceof List<?> ? new MathException("Failed to interpret math equation in variable-accessing: variable accessing may NOT return lists (no support).")
            : data instanceof NbtCompound nbt ? new NBTEP(nbt)
            : data instanceof ItemStack ? new MathException("Failed to interpret math equation in variable-accessing: variable accessing may NOT return item stacks (no support).")
            : data instanceof String string ? new StringEP(string)
            : data instanceof StatusEffectInstance ? new MathException("Failed to interpret math equation in variable-accessing: variable accessing may NOT return status effects (no support).")
            : data == null ? new NumberEP(0) // yes, the default case for non-existent variables (null) is 0.
            : new MathException("Oops! Big fucky wucky, a wittle fucko boingo. Contact Luxof: " + data.getClass().toString());
        if (data instanceof RuntimeException e) throw e;
        return (EquationPart)data;
    }
    /** Turns an Iota or EquationPart into its underlying value. */
    public static Object deIotaThisIotaIfPossible(Object data) {
        return data instanceof EquationPart ep
            ? (ep instanceof NumberEP numEp ? numEp.value
            : ep instanceof StringEP stringEp ? stringEp.value
            : ep instanceof VecEP vecEp ? vecEp.value
            : ep instanceof NBTEP nbtEp ? nbtEp.nbt : data)
            : data instanceof Iota
            ? (data instanceof NullIota ? null
            : data instanceof DoubleIota i ? i.getDouble()
            : data instanceof BooleanIota i ? i.getBool() ? 1.0 : 0.0
            : data instanceof EntityIota i && i.getEntity() instanceof LivingEntity living ? living
            : data instanceof ListIota i ? getList(i)
            : data instanceof Vec3Iota i ? i.getVec3() : data)
            : data;
    }

    private static List<Iota> getList(ListIota listIota) {
        List<Iota> list = new ArrayList<>();
        listIota.getList().forEach(list::add);
        return List.copyOf(list);
    }

    private static List<ItemStack> getEquipped(LivingEntity living) {
        EquipmentSlot[] armorSlots = EquipmentSlot.values();
        ItemStack[] armor = new ItemStack[armorSlots.length];
        for (int i = 0; i < armorSlots.length; i++) {
            armor[i] = living.getEquippedStack(armorSlots[i]);
        }
        return List.of(armor);
    }
}
