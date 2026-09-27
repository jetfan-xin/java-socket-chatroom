# Architecture

The application follows a classic desktop client-server design. A single TCP connection carries serialized request and response objects for account and chat events. A separate peer-to-peer socket is opened only after both users agree to a file transfer.

```mermaid
flowchart LR
    subgraph ClientA["Swing client A"]
        AUI["Login / chat UI"]
        ATH["Response listener"]
        AC["Client state"]
    end

    subgraph Server["Chat server"]
        SS["Server socket"]
        RP["Request processor"]
        OS["Online session registry"]
        US["User service"]
        SUI["Swing admin console"]
    end

    subgraph ClientB["Swing client B"]
        BUI["Login / chat UI"]
        BTH["Response listener"]
        BC["Client state"]
    end

    AUI --> AC --> SS
    SS --> RP
    RP <--> OS
    RP <--> US
    SUI --> OS
    RP --> ATH --> AUI
    RP --> BTH --> BUI
    BUI --> BC --> SS
    ClientA -. "approved file stream" .-> ClientB
```

## Message flow

```mermaid
sequenceDiagram
    participant A as Client A
    participant S as Server
    participant B as Client B

    A->>S: LOGIN request
    S-->>A: Current user + online users
    S-->>B: USER_LOGGED_IN event
    A->>S: CHAT request (public or private)
    S-->>B: CHAT event
    A->>S: FILE_OFFER request
    S-->>B: FILE_OFFER event
    B->>S: FILE_ACCEPT request
    S-->>A: FILE_ACCEPT event
    S-->>B: FILE_RECEIVE event
    A->>B: Buffered byte stream on transfer socket
```

## Deliberate boundaries

- This is a local-network learning project, not a production chat service.
- Java object serialization and an unencrypted socket protocol are retained from the coursework for provenance and simplicity.
- Runtime user data is stored locally under `data/` and is excluded from version control.
- Swing state is updated on the Event Dispatch Thread where the refactor touches asynchronous callbacks.
