module FrontendTest exposing (..)

import Frontend exposing (..)
import Expect exposing (equal)
import Test exposing (Test, test)
import Json.Decode exposing (decodeString)

decodesGameList : Test
decodesGameList =
    test "Properly decodes a game list" <|
        \() ->
            let
                json =
                    """
                      [
                          {
                              "gameId": "game1",
                              "player1": "player1",
                              "player2": "player2",
                              "winner": "player1",
                              "state": "ended"
                          },
                          {
                              "gameId": "game2",
                              "state": "joinable"
                          },
                          {
                              "gameId": "game3",
                              "player1": "player1",
                              "state": "joinable"
                          },
                          {
                              "gameId": "game4",
                              "player1": "player1",
                              "player2": "player2",
                              "state": "started"
                          }
                      ]
                    """

                decodedOutput  =
                    decodeString gameListDecoder json
            in
                equal
                    decodedOutput
                    (Ok
                        [ Game "game1" (Just <| Player "player1") (Just <| Player "player2") (Just <| Player "player1") Ended
                        , Game "game2" Nothing Nothing Nothing Joinable
                        , Game "game3" (Just <| Player "player1") Nothing Nothing Joinable
                        , Game "game4" (Just <| Player "player1") (Just <| Player "player2") Nothing Started]
                    )