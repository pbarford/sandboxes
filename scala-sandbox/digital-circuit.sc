type Wire

def inverter(in: Wire, out: Wire):Unit
def andGate(in1: Wire, In2: Wire, out: Wire): Unit
def orGate(in1: Wire, In2: Wire, out: Wire): Unit

def HalfAdder(a: Wire, b: Wire, s: Wire, c: Wire): Unit = {
  val d = new Wire
  val e = new Wire
  orGate(a, b, d)
  andGate(a, b, c)
  inverter(c, e)
  andGate(d, e, s)

}