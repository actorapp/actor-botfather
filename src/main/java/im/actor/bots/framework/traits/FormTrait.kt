package im.actor.bots.framework.traits

import im.actor.botkit.forms.*
import java.util.*

/**
 * Trait for forms
 */
final data class KForm(val name: String, val enabled: Boolean, val color: String, val inputs: List<KInput>) {
    fun toJson(): String {
        return Form(name, inputs.map { e -> e.toInput() }, enabled, color).toJson()
    }

    companion object {
        fun fromActionForm(aForm: ActionForm): KForm = KForm(aForm.name(), aForm.enabled(), aForm.color(), aForm.inputs.map { e -> KInput.toKInput(e) })
//        fun parse(json: String): Optional<KForm> {
//            val optForm = Form.parseOption(json)
//            return if (optForm.isDefined) {
//                val f = optForm.get()
//                Optional.of(KForm(f.name(), f.enabled(), f.color(), f.inputs.map { i -> KInput.toKInput(i) }))
//            } else {
//                Optional.empty<KForm>()
//            }
//        }
    }
}

interface KInput {
    fun toInput():Input
    companion object {
        fun toKInput(i: Input): KInput = when(i) {
            is TextInput -> KTextInput(i.enabled(), i.name(), i.label(), i.inputType(), i.data())
            is Button -> KButton(i.enabled(), i.name(), i.label())
            is Slider -> KSlider(i.enabled(), i.name(), i.label(), i.progress(), i.showHandle(), i.sendOnChange())
            is Label -> KLabel(i.enabled(), i.name(), i.label())
            is Checkbox -> KCheckbox(i.enabled(), i.name(), i.label(), i.checked())
            is DatePicker -> KDatePicker(i.enabled(), i.name(), i.label(), i.data(), i.ts(), i.pickerType())
            is ElementsList -> {
                val kElems = i.elems.map { e -> KElement(e.id(), e.value()) }
                KElementsList(i.enabled(), i.name(), i.label(), kElems, i.selected(),i.sendOnChange())
            }
            else -> KButton(i.enabled(), i.name(), i.label()) //TODO: fix with real data

        }
    }
}

final data class KTextInput(
        val enabled:   Boolean,
        val name:      String,
        val label:     String,
        val inputType: Int,
        val data:      String
): KInput {
    override fun toInput(): Input = TextInput(enabled, name, label, inputType, data)
}

final data class KButton(
        val enabled: Boolean,
        val name:    String,
        val label:   String
): KInput {
    override fun toInput(): Input = Button(enabled, name, label)
}

final data class KSlider(
        val enabled:      Boolean,
        val name:         String,
        val label:        String,
        val progress:     Int,
        val showHandle:   Boolean,
        val sendOnChange: Boolean
): KInput {
    override fun toInput(): Input = Slider(enabled, name, label, progress, showHandle, sendOnChange)
}

final data class KLabel(
        val enabled: Boolean,
        val name:    String,
        val label:   String
): KInput {
    override fun toInput(): Input = Label(enabled, name, label)
}

//???
final data class KCheckbox(
        val enabled: Boolean,
        val name:    String,
        val label:   String,
        val checked: Boolean
): KInput {
    override fun toInput(): Input = Checkbox(enabled, name, label, checked)
}

final data class KDatePicker(
        val enabled: Boolean,
        val name:    String,
        val label:   String,
        val data:    String,
        val ts:      Long,
        val pickerType: Int
): KInput {
    override fun toInput(): Input = DatePicker(enabled, name, label, data, ts, pickerType)
}

final data class KElement(val id: Int, val value: String) {
    companion object {
        fun toElement(ke: KElement): Element = Element(ke.id, ke.value)
    }
}

final data class KElementsList(
        val enabled:Boolean,
        val name:   String,
        val label:  String,
        val elems:  List<KElement>,
        val selected:     Int,
        val sendOnChange: Boolean
): KInput {
    override fun toInput(): Input = ElementsList(enabled, name, label, elems.map { e -> KElement.toElement(e) }, selected, sendOnChange)
}


interface FormTrait {
    // form name is form id
    fun makeForm(name: String, enabled:Boolean, color: String, vararg inputs: Input): Form
    fun parseForm(json: String): Form
}

class FormTraitImpl() : FormTrait {
    override fun makeForm(name: String, enabled: Boolean, color: String, vararg inputs: Input): Form = Form(name, inputs.toList(), enabled, color)
    override fun parseForm(json: String): Form = Form.parse(json)
}