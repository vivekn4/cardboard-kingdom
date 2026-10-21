"""Mock-test harness for Cardboard Kingdom M0 game math.

Ports android/.../GameState.kt EXACTLY (same constants, same order:
drains -> health drains -> clock advance -> day rollover; sleep/eat/
dumpster/kitchen/shelter with the same values and clamps).

Run:  python3 game_math_tests.py
Exit 0 = all green.
"""
import sys

# ---- constants copied from GameState.Companion ----
HUNGER_DRAIN = 2.0
THIRST_DRAIN = 2.5
ENERGY_DRAIN = 1.5
WARMTH_WINTER_NIGHT = 5.0
WARMTH_WINTER_DAY = 1.5
WARMTH_MILD_NIGHT = 2.0
WARMTH_MILD_DAY = 0.5
HP_STARVING = 4.0
HP_PARCHED = 5.0
HP_FROZEN = 6.0
HP_LOW = 1.0
MOVE_COST = 0.3
SLEEP_E_CAMP, SLEEP_E_ROUGH = 80.0, 40.0
SLEEP_H_CAMP, SLEEP_H_ROUGH = 15.0, 5.0
SLEEP_W_CAMP, SLEEP_W_ROUGH = 80.0, 50.0
SLEEP_HUNGER_COST = SLEEP_THIRST_COST = 15.0
EAT_HUNGER, EAT_THIRST = 30.0, 2.0
DUMPSTER_MAX = 3
DUMPSTER_E_COST = 2.0
KITCHEN_HUNGER, KITCHEN_THIRST = 50.0, 20.0
SHELTER_WARMTH = 40.0


def clamp(v):
    return max(0.0, min(100.0, v))


class State:
    def __init__(self, **kw):
        self.day = kw.get('day', 1)
        self.hour = kw.get('hour', 8)
        self.hunger = kw.get('hunger', 70.0)
        self.thirst = kw.get('thirst', 70.0)
        self.health = kw.get('health', 100.0)
        self.warmth = kw.get('warmth', 80.0)
        self.energy = kw.get('energy', 80.0)
        self.money = kw.get('money', 0)
        self.food = kw.get('food', 0)
        self.kitchen_used_day = kw.get('kitchen_used_day', 0)
        self.shelter_used_day = kw.get('shelter_used_day', 0)
        self.dumpster_today = kw.get('dumpster_today', 0)
        self.season = kw.get('season', 'winter')
        self.game_over = kw.get('game_over', False)

    def is_night(self):
        return self.hour >= 22 or self.hour < 6

    def tick_hour(self):
        if self.game_over:
            return
        self.hunger = clamp(self.hunger - HUNGER_DRAIN)
        self.thirst = clamp(self.thirst - THIRST_DRAIN)
        self.energy = clamp(self.energy - ENERGY_DRAIN)
        night = self.is_night()
        if self.season == 'winter':
            wd = WARMTH_WINTER_NIGHT if night else WARMTH_WINTER_DAY
        else:
            wd = WARMTH_MILD_NIGHT if night else WARMTH_MILD_DAY
        self.warmth = clamp(self.warmth - wd)
        hp = 0.0
        if self.hunger <= 0:
            hp += HP_STARVING
        if self.thirst <= 0:
            hp += HP_PARCHED
        if self.warmth <= 0:
            hp += HP_FROZEN
        if self.hunger < 20:
            hp += HP_LOW
        if self.thirst < 20:
            hp += HP_LOW
        self.health = clamp(self.health - hp)
        if self.health <= 0:
            self.game_over = True
        self.hour += 1
        if self.hour >= 24:
            self.hour = 0
            self.day += 1
            self.dumpster_today = 0

    def step_cost(self):
        if self.energy <= 0:
            return False
        self.energy = clamp(self.energy - MOVE_COST)
        return True

    def sleep(self, at_camp):
        self.energy = clamp(self.energy + (SLEEP_E_CAMP if at_camp else SLEEP_E_ROUGH))
        self.health = clamp(self.health + (SLEEP_H_CAMP if at_camp else SLEEP_H_ROUGH))
        target = SLEEP_W_CAMP if at_camp else SLEEP_W_ROUGH
        self.warmth = clamp(max(target, self.warmth))
        self.hunger = clamp(self.hunger - SLEEP_HUNGER_COST)
        self.thirst = clamp(self.thirst - SLEEP_THIRST_COST)
        self.day += 1
        self.hour = 8
        self.dumpster_today = 0

    def eat(self):
        if self.food <= 0:
            return False
        self.food -= 1
        self.hunger = clamp(self.hunger + EAT_HUNGER)
        self.thirst = clamp(self.thirst + EAT_THIRST)
        return True

    def search_dumpster(self):
        if self.dumpster_today >= DUMPSTER_MAX:
            return False
        self.dumpster_today += 1
        self.food += 1
        self.energy = clamp(self.energy - DUMPSTER_E_COST)
        return True

    def soup_kitchen(self):
        if self.kitchen_used_day == self.day:
            return False
        self.kitchen_used_day = self.day
        self.hunger = clamp(self.hunger + KITCHEN_HUNGER)
        self.thirst = clamp(self.thirst + KITCHEN_THIRST)
        return True

    def shelter_warm_up(self):
        if self.shelter_used_day == self.day:
            return False
        self.shelter_used_day = self.day
        self.warmth = clamp(self.warmth + SHELTER_WARMTH)
        return True


passed = failed = 0


def check(name, got, want):
    global passed, failed
    if got == want:
        passed += 1
    else:
        failed += 1
        print(f"  FAIL {name}: got {got}, want {want}")


def approx(name, got, want, eps=1e-6):
    check(name, abs(got - want) < eps, True)


print("== 1. Hourly drains (winter day, hour 8) ==")
s = State()
s.tick_hour()
approx("hunger -2", s.hunger, 68.0)
approx("thirst -2.5", s.thirst, 67.5)
approx("energy -1.5", s.energy, 78.5)
approx("warmth winter day -1.5", s.warmth, 78.5)
check("hour advances", s.hour, 9)

print("== 2. Winter night is brutal ==")
s = State(hour=23)
s.tick_hour()
approx("warmth winter night -5", s.warmth, 75.0)
s = State(hour=2)
s.tick_hour()
approx("warmth 2am -5", s.warmth, 75.0)
s = State(hour=5)
s.tick_hour()
approx("warmth 5am still night", s.warmth, 75.0)
s = State(hour=6)
s.tick_hour()
approx("warmth 6am day rate", s.warmth, 78.5)

print("== 3. Mild season drains less ==")
s = State(hour=23, season='summer')
s.tick_hour()
approx("mild night -2", s.warmth, 78.0)
s = State(hour=10, season='summer')
s.tick_hour()
approx("mild day -0.5", s.warmth, 79.5)

print("== 4. Clamping 0-100 ==")
s = State(hunger=1.0, thirst=1.0, energy=1.0, warmth=1.0)
s.tick_hour()
approx("hunger floored", s.hunger, 0.0)
approx("thirst floored", s.thirst, 0.0)
approx("energy floored", s.energy, 0.0)
approx("warmth floored", s.warmth, 0.0)
s = State(hunger=95.0)
s.soup_kitchen()
approx("hunger capped", s.hunger, 100.0)

print("== 5. Health drains stack ==")
s = State(hunger=0.0, thirst=0.0, warmth=0.0, health=100.0)
s.tick_hour()
approx("starving+parched+frozen+2x low = 17", s.health, 83.0)
s = State(hunger=10.0, thirst=10.0, warmth=50.0, health=100.0)
s.tick_hour()
approx("low hunger+thirst = 2", s.health, 98.0)
s = State(hunger=50.0, thirst=50.0, warmth=50.0, health=100.0)
s.tick_hour()
approx("healthy = no drain", s.health, 100.0)

print("== 6. Game over threshold ==")
s = State(health=5.0, hunger=0.0, thirst=0.0, warmth=0.0)
s.tick_hour()
check("game over at 0 hp", s.game_over, True)
check("hp floored at 0", s.health, 0.0)
s2 = State(health=5.0, hunger=0.0, thirst=0.0, warmth=0.0)
s2.tick_hour()
s2.tick_hour()  # frozen state: no further ticks after game over
check("no tick after game over", s2.day, 1)

print("== 7. Day rollover ==")
s = State(hour=23, day=3, dumpster_today=3)
s.tick_hour()
check("midnight -> hour 0", s.hour, 0)
check("day increments", s.day, 4)
check("dumpster resets", s.dumpster_today, 0)

print("== 8. Sleep: camp vs rough ==")
s = State(energy=10.0, health=50.0, warmth=20.0, hunger=50.0, thirst=50.0, day=2)
s.sleep(at_camp=True)
approx("camp energy +80", s.energy, 90.0)
approx("camp health +15", s.health, 65.0)
approx("camp warmth to 80", s.warmth, 80.0)
approx("sleep hunger -15", s.hunger, 35.0)
check("sleep -> next day 8am", (s.day, s.hour), (3, 8))
s = State(energy=10.0, health=50.0, warmth=20.0, day=2)
s.sleep(at_camp=False)
approx("rough energy +40", s.energy, 50.0)
approx("rough health +5", s.health, 55.0)
approx("rough warmth to 50", s.warmth, 50.0)
s = State(energy=95.0, health=98.0, warmth=95.0)
s.sleep(at_camp=True)
check("sleep caps at 100", (s.energy, s.health, s.warmth), (100.0, 100.0, 95.0))

print("== 9. Eat / dumpster / kitchen / shelter ==")
s = State(food=2, hunger=50.0, thirst=50.0)
check("eat ok", s.eat(), True)
approx("eat hunger +30", s.hunger, 80.0)
check("food decrements", s.food, 1)
s = State(food=0)
check("eat with no food fails", s.eat(), False)
s = State(energy=50.0)
for _ in range(3):
    check("dumpster find", s.search_dumpster(), True)
check("4th dumpster blocked", s.search_dumpster(), False)
check("dumpster energy cost", s.energy, 44.0)
s = State(day=5, hunger=10.0, thirst=10.0)
check("kitchen first meal", s.soup_kitchen(), True)
check("kitchen second blocked", s.soup_kitchen(), False)
s.day = 6
check("kitchen next day ok", s.soup_kitchen(), True)
s = State(day=5, warmth=10.0)
check("shelter warmup", s.shelter_warm_up(), True)
approx("shelter +40", s.warmth, 50.0)
check("shelter second blocked", s.shelter_warm_up(), False)

print("== 10. Movement cost ==")
s = State(energy=1.0)
check("step ok", s.step_cost(), True)
approx("step -0.3", s.energy, 0.7)
s = State(energy=0.0)
check("step at 0 energy blocked", s.step_cost(), False)

print("== 11. Winter-night worst case: 12h night survival ==")
s = State(hour=18, warmth=100.0, hunger=100.0, thirst=100.0, health=100.0)
for _ in range(12):  # 18:00 -> 06:00
    s.tick_hour()
    if s.game_over:
        break
print(f"  after 12h: warmth={s.warmth} health={s.health} game_over={s.game_over}")
# 18-21 day rate (4h x 1.5 = 6) + 22-05 night rate (8h x 5 = 40) = 46 lost.
check("survives one full winter night from full", s.game_over, False)
check("warmth after winter night", s.warmth, 54.0)
s = State(hour=18, warmth=30.0, hunger=100.0, thirst=100.0, health=100.0)
for _ in range(12):
    s.tick_hour()
    if s.game_over:
        break
print(f"  from warmth 30: warmth={s.warmth} health={s.health} game_over={s.game_over}")
check("low warmth hits 0 overnight", s.warmth, 0.0)
check("frostbite drains health", s.health < 100.0, True)

print("== 12. Full day loop sanity: eat+sleep keeps you alive ==")
s = State()
for day in range(1, 4):
    for _ in range(14):  # 8:00 -> 22:00
        s.tick_hour()
    s.soup_kitchen()
    s.sleep(at_camp=True)
check("3 days with meals+camp sleep: alive", s.game_over, False)
check("day counter", s.day, 4)

print(f"\n{passed} passed, {failed} failed")
sys.exit(1 if failed else 0)
