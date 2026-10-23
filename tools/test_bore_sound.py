from pathlib import Path
import struct

ROOT = Path(__file__).resolve().parent.parent
OVERRIDE = ROOT / "tools/assets_extra/sounds/rumble.ogg"
GENERATED = ROOT / "src/main/resources/assets/thaumcraft/sounds/rumble.ogg"
data = OVERRIDE.read_bytes()
header = data.index(b"\x01vorbis")
assert data[header + 11] == 1, "the bore rumble must be mono for positional attenuation"
assert struct.unpack_from("<I", data, header + 12)[0] == 44100, "rumble sample rate changed"
assert GENERATED.read_bytes() == data, "generated rumble differs from the mono source override"
print("Bore rumble: mono, 44100 Hz, generated asset matches source override")
