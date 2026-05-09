/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  async rewrites() {
    const gatewayUrl = process.env.API_GATEWAY_URL || 'http://localhost:8080'

    return [
      {
        source: '/api/:path*',
        destination: `${gatewayUrl}/:path*`,
      },
    ]
  },
}

export default nextConfig
