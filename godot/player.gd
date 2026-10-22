extends CharacterBody3D
## Player controller: keyboard (WASD/arrows) + virtual joystick vector.
## Camera follows from behind-above. Pushes RigidBody3D carts on contact.

const SPEED := 5.0

var joy_vector := Vector2.ZERO
var _cam: Camera3D
var _sprite: Sprite3D

func _ready() -> void:
	_cam = Camera3D.new()
	get_parent().add_child(_cam)
	_sprite = get_node_or_null("Sprite")
	if _sprite == null:
		_sprite = Sprite3D.new()
		_sprite.name = "Sprite"
		_sprite.position.y = 0.9
		add_child(_sprite)

func _physics_process(_delta: float) -> void:
	var ix := 0.0
	var iz := 0.0
	if Input.is_key_pressed(KEY_D) or Input.is_key_pressed(KEY_RIGHT):
		ix += 1.0
	if Input.is_key_pressed(KEY_A) or Input.is_key_pressed(KEY_LEFT):
		ix -= 1.0
	if Input.is_key_pressed(KEY_S) or Input.is_key_pressed(KEY_DOWN):
		iz += 1.0
	if Input.is_key_pressed(KEY_W) or Input.is_key_pressed(KEY_UP):
		iz -= 1.0
	ix += joy_vector.x
	iz += joy_vector.y
	var move := Vector3(ix, 0.0, iz)
	if move.length() > 1.0:
		move = move.normalized()
	velocity.x = move.x * SPEED
	velocity.z = move.z * SPEED
	velocity.y = 0.0
	move_and_slide()
	# Push physics bodies (shopping cart) on contact.
	for i in range(get_slide_collision_count()):
		var col := get_slide_collision(i)
		var body := col.get_collider()
		if body is RigidBody3D:
			body.apply_central_impulse(-col.get_normal() * 2.0)
	if _sprite != null and absf(move.x) > 0.1:
		_sprite.flip_h = move.x < 0.0

func _process(_delta: float) -> void:
	if _cam != null:
		var target := global_position + Vector3(0.0, 8.0, 7.0)
		_cam.position = _cam.position.lerp(target, 0.12)
		_cam.look_at(global_position + Vector3(0.0, 1.0, 0.0))
	if _sprite != null and _cam != null:
		var sp := _sprite.global_position
		var cp := _cam.global_position
		_sprite.rotation.y = atan2(cp.x - sp.x, cp.z - sp.z)

func get_camera() -> Camera3D:
	return _cam

func snap_camera() -> void:
	if _cam != null:
		_cam.position = global_position + Vector3(0.0, 8.0, 7.0)
		_cam.look_at(global_position + Vector3(0.0, 1.0, 0.0))
