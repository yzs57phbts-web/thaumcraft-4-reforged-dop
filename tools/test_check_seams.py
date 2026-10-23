import unittest

from check_seams import violated_rule


class ClientBoundaryTests(unittest.TestCase):
    def test_nested_client_renderer_is_recognized(self):
        self.assertIsNone(violated_rule("modern/client/CandleRenderer.java", "net.minecraft.client.renderer.MultiBufferSource"))

    def test_gameplay_cannot_import_client_renderer(self):
        self.assertIsNotNone(violated_rule("entity/golem/GolemBase.java", "net.minecraft.client.renderer.MultiBufferSource"))

    def test_geometry_still_rejects_minecraft_dependencies(self):
        self.assertIsNotNone(violated_rule("client/geom/Example.java", "net.minecraft.client.Minecraft"))


if __name__ == "__main__":
    unittest.main()
