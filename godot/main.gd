extends Node3D
## Cardboard Kingdom 3D — greybox (v0.1).
## 3D Frostgate, billboard sprites, M0 survival loop ported from Kotlin:
## stats, clock, dumpster/soup/shelter/bench/camp, begging, busking,
## game over with the 211 line, JSON autosave. Full systems in GDD v2.

const SAVE_PATH := "user://save.json"

# Stats (0..100, money in coins).
var hunger := 70.0
var thirst := 70.0
var warmth := 80.0
var energy := 80.0
var health := 100.0
var morale := 60.0
var money := 5
var respect := 0
var day := 1
var game_minutes := 480.0  # Start at 08:00.
var dumpster_uses := 0
var soup_used := false
var shelter_used := false

var _player: CharacterBody3D
var _near_kind := ""
var _near_areas := {}
var _msg: Label
var _stats_label: Label
var _clock_label: Label
var _prompt_label: Label
var _interact_btn: Button
var _gameover: ColorRect
var _rough_panel: Control
var _markers: Array = []
var _billboards: Array = []
var _time := 0.0
var _hud_timer := 0.0
# Screenshot tour (CK_SHOTS) + scripted QC test (CK_TEST) modes.
var _shot_mode := false
var _test_mode := false
var _shot_step := -1
var _shot_timer := 0.0
var _test_step := -1
var _test_timer := 0.0
var _test_log: Array = []
var _test_pass := 0
var _test_fail := 0
var _shot_views := [
	Vector3(0, 0, 4),
	Vector3(5, 0, -10),
	Vector3(-13, 0, -16),
	Vector3(10, 0, 0),
	Vector3(-8, 0, 12),
]
var _shot_names := ["spawn", "camp", "kitchen", "reyes", "town"]

const DIALOGUE := {
	"talk_june": "Mama June: \"Eat, hon. Soup's on. There's always a bowl for you here.\"",
	"talk_pete": "Pigeon Pete: \"The pigeons see everything. Feed them and they'll show you things.\"",
	"talk_reyes": "Officer Reyes: \"Keep it peaceful and we won't have a problem. Frostgate's cold enough already.\"",
	"talk_caw": "Caw: \"This turf's ours, Rook. You want it? Earn it.\"",
}

class JoyStick extends Control:
	var vec := Vector2.ZERO
	var _dragging := false
	var _center := Vector2.ZERO

	func _ready() -> void:
		size = Vector2(130, 130)

	func _gui_input(event: InputEvent) -> void:
		if event is InputEventScreenTouch:
			var touch := event as InputEventScreenTouch
			if touch.pressed:
				_dragging = true
				_center = size / 2.0
				_update_vec(touch.position)
			else:
				_dragging = false
				vec = Vector2.ZERO
				queue_redraw()
		elif event is InputEventMouseButton:
			var btn := event as InputEventMouseButton
			if btn.pressed:
				_dragging = true
				_center = size / 2.0
				_update_vec(btn.position)
			else:
				_dragging = false
				vec = Vector2.ZERO
				queue_redraw()
		elif event is InputEventScreenDrag:
			if _dragging:
				_update_vec((event as InputEventScreenDrag).position)
		elif event is InputEventMouseMotion:
			if _dragging:
				_update_vec((event as InputEventMouseMotion).position)

	func _update_vec(pos: Vector2) -> void:
		var off := pos - _center
		if off.length() > 48.0:
			off = off.normalized() * 48.0
		vec = off / 48.0
		queue_redraw()

	func _draw() -> void:
		var c := size / 2.0
		draw_circle(c, 52.0, Color(1, 1, 1, 0.15))
		draw_circle(c + vec * 40.0, 22.0, Color(1, 1, 1, 0.4))

var _joy: JoyStick

func _ready() -> void:
	randomize()
	_shot_mode = OS.has_environment("CK_SHOTS")
	_test_mode = OS.has_environment("CK_TEST")
	_build_world()
	_build_player()
	_build_npcs()
	_build_interactables()
	_build_hud()
	if not _test_mode:
		_load_save()
	_update_hud()
	if _test_mode:
		_test_step = 0
		_test_timer = 0.0
	if _shot_mode:
		_shot_step = 0
		_shot_timer = 0.0
		DirAccess.make_dir_recursive_absolute(OS.get_user_data_dir() + "/shots")

# ---------- world ----------

func _mat(color: Color) -> StandardMaterial3D:
	var m := StandardMaterial3D.new()
	m.albedo_color = color
	return m

func _box_mesh(size: Vector3, color: Color) -> MeshInstance3D:
	var mi := MeshInstance3D.new()
	var mesh := BoxMesh.new()
	mesh.size = size
	mi.mesh = mesh
	mi.material_override = _mat(color)
	return mi

func _place(node: Node3D, x: float, y: float, z: float) -> void:
	node.position = Vector3(x, y, z)
	add_child(node)

func _building(x: float, z: float, w: float, h: float, d: float, color: Color, label_text: String) -> void:
	var b := _box_mesh(Vector3(w, h, d), color)
	_place(b, x, h / 2.0, z)
	var roof := _box_mesh(Vector3(w + 0.6, 0.5, d + 0.6), Color(0.25, 0.27, 0.3))
	_place(roof, x, h + 0.25, z)
	var sign := Label3D.new()
	sign.text = label_text
	sign.position = Vector3(x, h + 1.4, z)
	add_child(sign)

func _tree(x: float, z: float) -> void:
	# Billboard tree sprite (Kenney Tiny Town) instead of geometry.
	var tex_path := "res://assets/objects/tree_pine.png"
	if int(abs(x + z)) % 2 == 0:
		tex_path = "res://assets/objects/tree_leafy.png"
	var s := Sprite3D.new()
	s.texture = load(tex_path)
	s.pixel_size = 0.02
	s.position = Vector3(x, 1.28, z)
	add_child(s)
	_billboards.append(s)

func _build_world() -> void:
	var env := WorldEnvironment.new()
	var e := Environment.new()
	e.background_mode = Environment.BG_COLOR
	e.background_color = Color(0.62, 0.72, 0.85)
	e.ambient_light_source = Environment.AMBIENT_SOURCE_COLOR
	e.ambient_light_color = Color(0.7, 0.75, 0.8)
	e.ambient_light_energy = 0.5
	env.environment = e
	add_child(env)
	var sun := DirectionalLight3D.new()
	sun.name = "Sun"
	sun.light_energy = 0.9
	sun.rotation_degrees = Vector3(-50, 30, 0)
	add_child(sun)
	var ground := MeshInstance3D.new()
	var plane := PlaneMesh.new()
	plane.size = Vector2(140, 140)
	ground.mesh = plane
	ground.material_override = _mat(Color(0.82, 0.86, 0.9))
	add_child(ground)
	# River strip + bridge.
	var river := _box_mesh(Vector3(140, 0.1, 7), Color(0.45, 0.65, 0.85))
	_place(river, 0, 0.02, -11)
	var deck := _box_mesh(Vector3(5, 0.4, 14), Color(0.5, 0.42, 0.33))
	_place(deck, 0, 0.8, -11)
	var pillar := _box_mesh(Vector3(4, 1.0, 1.0), Color(0.4, 0.4, 0.45))
	_place(pillar, 0, 0.4, -14)
	_place(pillar.duplicate(), 0, 0.4, -8)
	# Buildings.
	_building(-15, -20, 12, 5, 8, Color(0.75, 0.62, 0.45), "SOUP KITCHEN")
	_building(18, -18, 12, 5, 8, Color(0.6, 0.66, 0.72), "SHELTER")
	_building(-20, 10, 10, 4.5, 8, Color(0.55, 0.45, 0.6), "PAWN SHOP")
	_building(26, 12, 11, 5, 9, Color(0.4, 0.4, 0.42), "WAREHOUSE")
	# Camp under the bridge.
	var tent_a := _box_mesh(Vector3(2.6, 0.15, 2.2), Color(0.72, 0.6, 0.38))
	tent_a.rotation.z = 0.6
	_place(tent_a, 4.4, 0.8, -10)
	var tent_b := _box_mesh(Vector3(2.6, 0.15, 2.2), Color(0.65, 0.53, 0.32))
	tent_b.rotation.z = -0.6
	_place(tent_b, 5.6, 0.8, -10)
	var barrel := MeshInstance3D.new()
	var cyl := CylinderMesh.new()
	cyl.top_radius = 0.45
	cyl.bottom_radius = 0.45
	cyl.height = 0.9
	barrel.mesh = cyl
	barrel.material_override = _mat(Color(0.3, 0.3, 0.33))
	_place(barrel, 5, 0.45, -9)
	# Dumpsters, benches, trees.
	for pos in [Vector2(-8, 5), Vector2(12, 8)]:
		var d := _box_mesh(Vector3(2.0, 1.2, 1.1), Color(0.2, 0.35, 0.25))
		_place(d, pos.x, 0.6, pos.y)
		var lid := _box_mesh(Vector3(2.1, 0.15, 1.2), Color(0.16, 0.28, 0.2))
		_place(lid, pos.x, 1.25, pos.y)
	for pos in [Vector2(0, 6), Vector2(8, -2)]:
		var seat := _box_mesh(Vector3(2.0, 0.15, 0.6), Color(0.45, 0.32, 0.2))
		_place(seat, pos.x, 0.5, pos.y)
	for pos in [Vector2(-25, -5), Vector2(-18, 18), Vector2(14, -8), Vector2(22, -2), Vector2(-6, -22), Vector2(30, 2)]:
		_tree(pos.x, pos.y)

func _build_player() -> void:
	_player = CharacterBody3D.new()
	_player.name = "Player"
	var script: GDScript = load("res://player.gd")
	_player.set_script(script)
	var shape := CollisionShape3D.new()
	var cap := CapsuleShape3D.new()
	cap.radius = 0.4
	cap.height = 1.1
	shape.shape = cap
	shape.position.y = 0.9
	_player.add_child(shape)
	var sprite := Sprite3D.new()
	sprite.name = "Sprite"
	sprite.texture = load("res://sprites/rook.png")
	sprite.pixel_size = 0.007
	sprite.position.y = 0.9
	_player.add_child(sprite)
	_player.position = Vector3(0, 0, 4)
	add_child(_player)
	# Pushable shopping cart.
	var cart := RigidBody3D.new()
	cart.name = "Cart"
	cart.mass = 6.0
	cart.linear_damp = 1.0
	var cart_mesh := _box_mesh(Vector3(1.0, 0.7, 0.6), Color(0.55, 0.55, 0.6))
	cart.add_child(cart_mesh)
	var cart_shape := CollisionShape3D.new()
	var cart_box := BoxShape3D.new()
	cart_box.size = Vector3(1.0, 0.7, 0.6)
	cart_shape.shape = cart_box
	cart.add_child(cart_shape)
	cart.position = Vector3(2, 0.5, 2)
	add_child(cart)

func _npc_sprite(tex_path: String, x: float, z: float, name_text: String) -> void:
	var holder := Node3D.new()
	holder.position = Vector3(x, 0, z)
	var s := Sprite3D.new()
	s.texture = load(tex_path)
	s.pixel_size = 0.007
	s.position.y = 0.9
	holder.add_child(s)
	_billboards.append(s)
	var label := Label3D.new()
	label.text = name_text
	label.position.y = 2.1
	holder.add_child(label)
	add_child(holder)

func _build_npcs() -> void:
	_npc_sprite("res://sprites/june.png", -13, -16, "Mama June")
	_npc_sprite("res://sprites/pete.png", 3, -11, "Pigeon Pete")
	_npc_sprite("res://sprites/reyes.png", 10, 0, "Officer Reyes")
	_npc_sprite("res://sprites/caw.png", 25, 10, "Caw")

func _make_area(kind: String, x: float, z: float) -> void:
	var area := Area3D.new()
	area.position = Vector3(x, 1.0, z)
	var shape := CollisionShape3D.new()
	var box := BoxShape3D.new()
	box.size = Vector3(2.0, 3.0, 2.0)
	shape.shape = box
	area.add_child(shape)
	area.body_entered.connect(_on_area_entered.bind(kind, area.position))
	area.body_exited.connect(_on_area_exited.bind(kind))
	add_child(area)
	var marker := MeshInstance3D.new()
	var sphere := SphereMesh.new()
	sphere.radius = 0.18
	sphere.height = 0.36
	marker.mesh = sphere
	var mm := StandardMaterial3D.new()
	mm.albedo_color = Color(1.0, 0.8, 0.2)
	mm.emission_enabled = true
	mm.emission = Color(1.0, 0.8, 0.2)
	marker.material_override = mm
	marker.position = Vector3(x, 1.8, z)
	add_child(marker)
	_markers.append(marker)

func _build_interactables() -> void:
	_make_area("dumpster", -8, 5)
	_make_area("dumpster", 12, 8)
	_make_area("soup", -13, -16)
	_make_area("shelter", 16, -16)
	_make_area("bench", 0, 6)
	_make_area("bench", 8, -2)
	_make_area("camp", 5, -10)
	_make_area("beg", -5, 12)
	_make_area("busk", 6, 14)
	_make_area("barrel", 5, -9)
	_make_area("talk_june", -11, -16)
	_make_area("talk_pete", 1, -11)
	_make_area("talk_reyes", 10, 0)
	_make_area("talk_caw", 25, 10)

# ---------- HUD ----------

func _label(x: float, y: float, w: float, h: float, size: int) -> Label:
	var l := Label.new()
	l.position = Vector2(x, y)
	l.size = Vector2(w, h)
	l.add_theme_font_size_override("font_size", size)
	return l

func _build_hud() -> void:
	var layer := CanvasLayer.new()
	add_child(layer)
	_stats_label = _label(12, 12, 420, 90, 20)
	layer.add_child(_stats_label)
	_clock_label = _label(520, 12, 190, 30, 20)
	layer.add_child(_clock_label)
	_prompt_label = _label(180, 1080, 380, 30, 22)
	layer.add_child(_prompt_label)
	_msg = _label(12, 1140, 700, 60, 20)
	layer.add_child(_msg)
	_interact_btn = Button.new()
	_interact_btn.text = "INTERACT (E)"
	_interact_btn.position = Vector2(540, 1170)
	_interact_btn.size = Vector2(170, 60)
	_interact_btn.pressed.connect(_on_interact_pressed)
	layer.add_child(_interact_btn)
	var rough_btn := Button.new()
	rough_btn.text = "SLEEP ROUGH"
	rough_btn.position = Vector2(360, 1170)
	rough_btn.size = Vector2(170, 60)
	rough_btn.pressed.connect(_on_rough_pressed)
	layer.add_child(rough_btn)
	_joy = JoyStick.new()
	_joy.position = Vector2(20, 1100)
	layer.add_child(_joy)
	# Rough-sleep confirm panel (hidden).
	_rough_panel = Control.new()
	_rough_panel.visible = false
	var bg := ColorRect.new()
	bg.color = Color(0, 0, 0, 0.8)
	bg.size = Vector2(720, 1280)
	_rough_panel.add_child(bg)
	var q := _label(110, 520, 500, 60, 24)
	q.text = "Sleep rough? Half restore, day passes."
	_rough_panel.add_child(q)
	var yes := Button.new()
	yes.text = "YES, SLEEP"
	yes.position = Vector2(180, 600)
	yes.size = Vector2(160, 60)
	yes.pressed.connect(_on_rough_yes)
	_rough_panel.add_child(yes)
	var no := Button.new()
	no.text = "CANCEL"
	no.position = Vector2(380, 600)
	no.size = Vector2(160, 60)
	no.pressed.connect(_on_rough_no)
	_rough_panel.add_child(no)
	layer.add_child(_rough_panel)
	# Game-over overlay (hidden).
	_gameover = ColorRect.new()
	_gameover.color = Color(0.05, 0.05, 0.08, 0.95)
	_gameover.size = Vector2(720, 1280)
	_gameover.visible = false
	var go := _label(60, 420, 600, 200, 26)
	go.text = "You didn't make it.\n\nDays survived: 0\n\nIf you or someone you know needs shelter help in Canada, call or text 211."
	go.name = "GameOverText"
	_gameover.add_child(go)
	var restart := Button.new()
	restart.text = "START OVER"
	restart.position = Vector2(270, 660)
	restart.size = Vector2(180, 60)
	restart.pressed.connect(_on_restart_pressed)
	_gameover.add_child(restart)
	layer.add_child(_gameover)

# ---------- interaction ----------

func _on_area_entered(body: Node3D, kind: String, pos: Vector3) -> void:
	if body == _player:
		_near_areas[kind] = pos
		_recompute_near()

func _on_area_exited(body: Node3D, kind: String) -> void:
	if body == _player:
		_near_areas.erase(kind)
		_recompute_near()

func _recompute_near() -> void:
	if _near_areas.is_empty() or _player == null:
		_near_kind = ""
		_prompt_label.text = ""
		return
	var best := ""
	var best_d := 1e9
	for kind in _near_areas.keys():
		var d := (_near_areas[kind] as Vector3).distance_to(_player.position)
		if d < best_d:
			best_d = d
			best = kind
	_near_kind = best
	_prompt_label.text = "Near: " + best + " — press INTERACT"

func _input(event: InputEvent) -> void:
	if event is InputEventKey and event.pressed and not event.echo and event.keycode == KEY_E:
		_do_interact()

func _on_interact_pressed() -> void:
	_do_interact()

func _say(text: String) -> void:
	_msg.text = text

func _do_interact() -> void:
	match _near_kind:
		"dumpster":
			if dumpster_uses < 3:
				dumpster_uses += 1
				hunger = minf(100.0, hunger + 25.0)
				money += randi_range(1, 5)
				_say("Found food in the dumpster. (%d/3 today)" % dumpster_uses)
			else:
				_say("Nothing left in the dumpsters today.")
		"soup":
			if not soup_used:
				soup_used = true
				hunger = minf(100.0, hunger + 45.0)
				thirst = minf(100.0, thirst + 20.0)
				_say("Mama June serves you a hot meal. Hunger restored.")
			else:
				_say("The kitchen already served you today.")
		"shelter":
			if not shelter_used:
				shelter_used = true
				warmth = 100.0
				energy = minf(100.0, energy + 20.0)
				_say("You warm up at the shelter.")
			else:
				_say("You've already warmed up here today.")
		"bench":
			energy = minf(100.0, energy + 40.0)
			warmth = minf(100.0, warmth + 10.0)
			_say("You rest on the bench.")
		"camp":
			_sleep_full()
		"beg":
			if energy > 10.0:
				money += randi_range(2, 8)
				morale = minf(100.0, morale + 2.0)
				energy -= 5.0
				_say("You beg with your sign. Someone spares change.")
			else:
				_say("Too tired to beg.")
		"busk":
			if energy > 10.0:
				money += randi_range(3, 10)
				morale = minf(100.0, morale + 5.0)
				energy -= 10.0
				_say("You busk a tune. Coins land in the case.")
			else:
				_say("Too tired to busk.")
		"barrel":
			warmth = minf(100.0, warmth + 30.0)
			_say("The fire barrel warms you.")
		"talk_june", "talk_pete", "talk_reyes", "talk_caw":
			_say(DIALOGUE[_near_kind])
		_:
			_say("Nothing to interact with here.")
	_update_hud()

func _sleep_full() -> void:
	hunger = 100.0
	thirst = 100.0
	warmth = 100.0
	energy = 100.0
	health = 100.0
	day += 1
	respect += 1
	game_minutes = 480.0
	dumpster_uses = 0
	soup_used = false
	shelter_used = false
	_save()
	_say("You slept at camp. Day %d begins." % day)
	_update_hud()

func _on_rough_pressed() -> void:
	_rough_panel.visible = true

func _on_rough_no() -> void:
	_rough_panel.visible = false

func _on_rough_yes() -> void:
	_rough_panel.visible = false
	hunger = hunger + (100.0 - hunger) / 2.0
	warmth = warmth + (100.0 - warmth) / 2.0
	energy = energy + (100.0 - energy) / 2.0
	day += 1
	game_minutes = 480.0
	dumpster_uses = 0
	soup_used = false
	shelter_used = false
	_save()
	_say("You slept rough. Half restored. Day %d begins." % day)
	_update_hud()

func _on_restart_pressed() -> void:
	_gameover.visible = false
	hunger = 70.0
	thirst = 70.0
	warmth = 80.0
	energy = 80.0
	health = 100.0
	morale = 60.0
	money = 5
	respect = 0
	day = 1
	game_minutes = 480.0
	dumpster_uses = 0
	soup_used = false
	shelter_used = false
	if _player != null:
		_player.position = Vector3(0, 0, 4)
	_save()
	_say("A new start in Frostgate.")
	_update_hud()

# ---------- screenshot tour + scripted QC test ----------

func _shot_update(delta: float) -> void:
	if _shot_step < 0 or _shot_step >= _shot_views.size():
		return
	if _shot_timer == 0.0 and _player != null:
		_player.position = _shot_views[_shot_step]
		_player.velocity = Vector3.ZERO
		_player.call("snap_camera")
	_shot_timer += delta
	if _shot_timer >= 1.6:
		var last := _shot_step == _shot_views.size() - 1
		_capture_shot(_shot_names[_shot_step])
		_shot_step += 1
		_shot_timer = 0.0
		if last:
			get_tree().quit()

func _capture_shot(name: String) -> void:
	# Synchronous: the viewport texture already holds the current view.
	var img := get_viewport().get_texture().get_image()
	var path := OS.get_user_data_dir() + "/shots/" + name + ".png"
	img.save_png(path)
	print("SHOT saved: ", path)

func _test_check(name: String, cond: bool) -> void:
	if cond:
		_test_pass += 1
		_test_log.append("PASS " + name)
		print("QC PASS: ", name)
	else:
		_test_fail += 1
		_test_log.append("FAIL " + name)
		print("QC FAIL: ", name)

func _test_next() -> void:
	_test_step += 1
	_test_timer = 0.0

func _finish_tests() -> void:
	var summary := "QC RESULT: %d passed, %d failed" % [_test_pass, _test_fail]
	print(summary)
	var f := FileAccess.open("user://qc_report.txt", FileAccess.WRITE)
	if f != null:
		for line in _test_log:
			f.store_line(line)
		f.store_line(summary)
	_test_step = -1
	get_tree().quit()

func _test_update(delta: float) -> void:
	if _test_step < 0:
		return
	_test_timer += delta
	match _test_step:
		0:
			if _test_timer >= 0.5:
				_test_check("initial hunger 70", int(hunger) == 70)
				_test_check("initial day 1", day == 1)
				_player.position = Vector3(-8, 0, 5)
				_test_next()
		1:
			if _test_timer >= 0.6:
				_test_check("near dumpster", _near_kind == "dumpster")
				_do_interact()
				_test_check("dumpster uses 1", dumpster_uses == 1)
				_test_check("hunger rose", hunger > 70.0)
				_player.position = Vector3(-13, 0, -16)
				_test_next()
		2:
			if _test_timer >= 0.6:
				_test_check("near soup (not talk)", _near_kind == "soup")
				_do_interact()
				_test_check("soup used", soup_used)
				_test_check("hunger higher after soup", hunger > 90.0)
				_player.position = Vector3(0, 0, 6)
				_test_next()
		3:
			if _test_timer >= 0.6:
				_test_check("near bench", _near_kind == "bench")
				_do_interact()
				_test_check("energy full after rest", int(energy) == 100)
				_player.position = Vector3(-5, 0, 12)
				_test_next()
		4:
			if _test_timer >= 0.6:
				_test_check("near beg spot", _near_kind == "beg")
				_do_interact()
				_test_check("money grew from begging", money > 5)
				_player.position = Vector3(-11, 0, -16)
				_test_next()
		5:
			if _test_timer >= 0.6:
				_test_check("near June talk spot", _near_kind == "talk_june")
				_do_interact()
				_test_check("June dialogue shown", _msg.text.contains("Mama June"))
				_player.position = Vector3(5, 0, -10)
				_test_next()
		6:
			if _test_timer >= 0.6:
				_test_check("near camp", _near_kind == "camp")
				_do_interact()
				_test_check("day 2 after sleep", day == 2)
				_test_check("stats full after sleep", int(hunger) == 100 and int(energy) == 100)
				_test_check("daily uses reset", dumpster_uses == 0 and not soup_used)
				_save()
				hunger = 1.0
				day = 9
				_load_save()
				_test_check("save/load roundtrip", day == 2 and int(hunger) == 100)
				health = 0.0
				_hourly_tick()
				_test_check("game over shows at 0 health", _gameover.visible)
				_finish_tests()

# ---------- clock, survival, save ----------

func _process(delta: float) -> void:
	_time += delta
	if _shot_mode:
		_shot_update(delta)
	if _test_mode:
		_test_update(delta)
	# Cylindrical billboards: sprites stay upright, face the camera.
	if _player != null:
		var cam = _player.call("get_camera")
		if cam != null:
			var cp = cam.global_position
			for s in _billboards:
				var sp = (s as Sprite3D).global_position
				(s as Sprite3D).rotation.y = atan2(cp.x - sp.x, cp.z - sp.z)
	game_minutes += delta  # 1 real second = 1 game minute.
	if game_minutes >= 1440.0:
		game_minutes -= 1440.0
		day += 1
		dumpster_uses = 0
		soup_used = false
		shelter_used = false
	# Bob the interactable markers.
	for i in range(_markers.size()):
		var m: Node3D = _markers[i]
		m.position.y = 1.8 + sin(_time * 2.0 + float(i)) * 0.2
	# Joystick -> player.
	if _player != null and _joy != null:
		_player.set("joy_vector", _joy.vec)
	# Hourly survival tick (60 game-minutes = 60 real seconds).
	_hud_timer += delta
	if _hud_timer >= 60.0:
		_hud_timer = 0.0
		_hourly_tick()
		_save()
	_update_hud_throttled(delta)

var _hud_acc := 0.0

func _update_hud_throttled(delta: float) -> void:
	_hud_acc += delta
	if _hud_acc >= 0.25:
		_hud_acc = 0.0
		_update_hud()

func _hourly_tick() -> void:
	var hour := int(game_minutes) / 60
	var night := hour < 6 or hour >= 20
	hunger = maxf(0.0, hunger - 4.0)
	thirst = maxf(0.0, thirst - 3.0)
	warmth = maxf(0.0, warmth - (6.0 if night else 3.0))
	energy = maxf(0.0, energy - 2.0)
	if hunger <= 0.0 or warmth <= 0.0:
		health = maxf(0.0, health - 5.0)
	if health <= 0.0 and not _gameover.visible:
		var t: Label = _gameover.get_node("GameOverText")
		t.text = "You didn't make it.\n\nDays survived: %d\n\nIf you or someone you know needs shelter help in Canada, call or text 211." % day
		_gameover.visible = true

func _update_hud() -> void:
	if _stats_label == null:
		return
	_stats_label.text = "Hunger %d  Warmth %d  Energy %d  Health %d\nMoney $%d  Morale %d  Respect %d" % [
		int(hunger), int(warmth), int(energy), int(health), money, int(morale), respect]
	var hh := int(game_minutes) / 60
	var mm := int(game_minutes) % 60
	_clock_label.text = "Day %d — %02d:%02d" % [day, hh, mm]

func _save() -> void:
	var data := {
		"day": day, "minutes": game_minutes,
		"hunger": hunger, "thirst": thirst, "warmth": warmth,
		"energy": energy, "health": health, "morale": morale,
		"money": money, "respect": respect,
	}
	var f := FileAccess.open(SAVE_PATH, FileAccess.WRITE)
	if f != null:
		f.store_string(JSON.stringify(data))

func _load_save() -> void:
	if not FileAccess.file_exists(SAVE_PATH):
		return
	var f := FileAccess.open(SAVE_PATH, FileAccess.READ)
	if f == null:
		return
	var parsed: Variant = JSON.parse_string(f.get_buffer(f.get_length()).get_string_from_utf8())
	if typeof(parsed) != TYPE_DICTIONARY:
		return
	var d: Dictionary = parsed
	day = int(d.get("day", 1))
	game_minutes = float(d.get("minutes", 480.0))
	hunger = float(d.get("hunger", 70.0))
	thirst = float(d.get("thirst", 70.0))
	warmth = float(d.get("warmth", 80.0))
	energy = float(d.get("energy", 80.0))
	health = float(d.get("health", 100.0))
	morale = float(d.get("morale", 60.0))
	money = int(d.get("money", 5))
	respect = int(d.get("respect", 0))
	_say("Save loaded. Welcome back, Rook.")
