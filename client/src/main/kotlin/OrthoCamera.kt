import vision.gears.webglmath.UniformProvider
import vision.gears.webglmath.Vec2
import vision.gears.webglmath.Mat4

class OrthoCamera() : UniformProvider("camera") {

  val position = Vec2(0.0f, 0.0f) 
  var yaw = 0.0f
  val windowSize = Vec2(4.0f, 4.0f)
    
  val viewProjMatrix by Mat4()
  val viewProjMatrixInverse by Mat4()  
  init{
    update()
  }

  fun update() { 
    viewProjMatrix.set(). 
      scale(0.5f, 0.5f, 1.0f). 
      scale(windowSize). 
      rotate(yaw).
      translate(position). 
      invert()
    viewProjMatrixInverse.set(). 
      scale(0.5f, 0.5f, 1.0f). 
      scale(windowSize). 
      rotate(yaw).
      translate(position)
  }

  fun setAspectRatio(ar : Float) { 
    windowSize.x = windowSize.y * ar
    update()
  } 
}