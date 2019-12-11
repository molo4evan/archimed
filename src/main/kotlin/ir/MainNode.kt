package ir

class MainNode(block: Block) : Block() {
    init {
        statements.addAll(block.statements)
    }

    constructor(): this(Block())
}