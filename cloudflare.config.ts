import { bindings, defineConfig } from "cf/config";

export default defineConfig({
	worker: {
		name: "mataku-today",
		compatibilityDate: "2026-02-13",
		entrypoint: "worker/entry.js",
		observability: {
			enabled: true,
		},
		assets: {
			runWorkerFirst: false,
		},
		env: {
			ASSETS: bindings.assets(),
		},
	},
});
