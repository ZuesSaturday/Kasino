package DAROARA.Casino.Kasino.Core.game.Record;

import DAROARA.Casino.Kasino.Core.KasinoCore.Card;

// Every move plays exactly one card from the current player's hand.
public sealed interface Move
        permits TrailMove, CaptureMove, BuildMove, RaiseMove, AddSetMove ,StandMove{
    Card played();
}







