import vision.gears.webglmath.*

class Bullet(mass: Float, momentOfInertia: Float, dragCoeffs: Vec2, vararg meshes: Mesh):
    PhysicsObject(mass, momentOfInertia, dragCoeffs, *meshes) {
    override val tag = Tag.BULLET
}