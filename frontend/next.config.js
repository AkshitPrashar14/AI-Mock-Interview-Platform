/** @type {import('next').NextConfig} */
const nextConfig = {
  // Output standalone for Docker multi-stage builds
  output: "standalone",

  // Strict mode for React
  reactStrictMode: true,

  // Environment variables exposed to the browser
  // All public env vars must be prefixed with NEXT_PUBLIC_
  env: {
    // NEXT_PUBLIC_API_URL: process.env.NEXT_PUBLIC_API_URL,
  },

  // Experimental features (placeholder)
  // experimental: {},
};

module.exports = nextConfig;
