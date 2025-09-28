class Explosion(
    spriteProgram: Program,
    geom: TexturedQuadGeometry,
    spriteTex: Texture2D,
    private val cols: Int = 6,
    private val rows: Int = 6,
    private val totalFrames: Int = 34,
    private val fps: Float = 24f,
    private val mat: Material = Material(spriteProgram).apply {
        this["colorTexture"]?.set(spriteTex)
        this["tint"]?.set(1f, 1f, 1f, 1f)
        this["cols"]?.set(cols.toFloat())
        this["rows"]?.set(rows.toFloat())
        this["frame"]?.set(0f)
    }
) : GameObject(Mesh(mat, geom)) {

    private var age = 0f
    private var frame = 0
    var aliveFx = true
        private set

    fun advance(dt: Float): Boolean {
        age += dt
        val f = kotlin.math.min((age * fps).toInt(), totalFrames - 1)
        if (f != frame) {
            frame = f
            mat["frame"]?.set(frame.toFloat())
        }
        if (frame >= totalFrames - 1) aliveFx = false
        update()
        return aliveFx
    }
}
