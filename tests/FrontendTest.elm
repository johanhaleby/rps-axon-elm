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
                              "gameId": "game",
                              "state": "joinable"
                          }
                      ]
                    """

                decodedOutput  =
                    decodeString gameListDecoder json
            in
                equal
                    decodedOutput
                    (Ok
                        [ Game "game" Nothing (Just <| Player "ikk2") (Just <| Player "ikk3") Joinable ]
                    )