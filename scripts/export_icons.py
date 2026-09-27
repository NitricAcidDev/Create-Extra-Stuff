"""Export the existing PSB merged preview without modifying either source file."""
from pathlib import Path
import struct
from PIL import Image

def export(source, target):
    with open(source, 'rb') as stream:
        magic, version, _, channels, height, width, depth, mode = struct.unpack('>4sH6sHIIHH', stream.read(26))
        assert (magic, version, depth, mode) == (b'8BPS', 2, 8, 3)
        for length_bytes in (4, 4, 8):
            length = int.from_bytes(stream.read(length_bytes), 'big')
            stream.seek(length, 1)
        assert int.from_bytes(stream.read(2), 'big') == 1
        lengths = [int.from_bytes(stream.read(4), 'big') for _ in range(channels * height)]
        planes = []
        for channel in range(channels):
            plane = bytearray()
            for row in range(height):
                data = stream.read(lengths[channel * height + row]); decoded = bytearray(); pos = 0
                while pos < len(data):
                    count = data[pos]; pos += 1
                    if count < 128:
                        decoded.extend(data[pos:pos+count+1]); pos += count+1
                    elif count > 128:
                        decoded.extend(data[pos:pos+1] * (257-count)); pos += 1
                assert len(decoded) == width
                plane.extend(decoded)
            planes.append(Image.frombytes('L', (width, height), bytes(plane)))
        image = Image.merge('RGBA' if channels == 4 else 'RGB', planes[:4])
        image.thumbnail((512, 512))
        image.save(target)

if __name__ == '__main__':
    root = Path(__file__).resolve().parent.parent
    export(root/'Fuel.psb', root/'create-factory-tweaks/src/main/resources/factory-tweaks-logo.png')
    export(root/'Tools.psb', root/'create-creative-tools-fabrications/src/main/resources/icon.png')
