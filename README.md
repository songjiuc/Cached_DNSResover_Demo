# Cached_DNSResover
A caching DNS resolver and server

Main features:
- Implement Java DatagramSocket class to create a server to listen for client DNS queries, generate and send response through UDP packets.
- Parse and store client queries, look for valid IP addresses in the local cache, build response packets and send back to the client.
- Forward the query to Google’s public DNS server if no valid IP address found. Parse and store Google’s response in the local cache and send it back to the client.
