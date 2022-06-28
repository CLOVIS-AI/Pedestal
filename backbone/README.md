# Pedestal Backbone • A common API structure

A regular REST API suffers from the following issues:
- most endpoints send recursive data, which often include information that the client already has,
- no caching capabilities,
- difficult integration with reactive UI frameworks.

Pedestal Backbone solves these issues by integrating cache management directly into the API to reduce transfer volume.
The client code can easily subscribe to API events using their favorite reactive UI framework for easy state management.
