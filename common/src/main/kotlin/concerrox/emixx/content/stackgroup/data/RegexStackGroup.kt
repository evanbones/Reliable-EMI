package concerrox.emixx.content.stackgroup.data

import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

class RegexStackGroup(
    id: ResourceLocation,
    private val regex: Regex,
    name: Component? = null
) : StackGroup(id, name) {

    override fun match(stack: EmiIngredient): Boolean {
        if (stack !is EmiStack) return false

        val stackId = stack.id.toString()

        return regex.matches(stackId)
    }
}