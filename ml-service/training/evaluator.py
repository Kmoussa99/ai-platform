"""Évaluation simple : perplexity, accuracy sur jeu de validation."""
import math


def perplexity(losses):
    if not losses:
        return None
    avg = sum(losses) / len(losses)
    return math.exp(avg)


def accuracy(preds, labels):
    if not preds:
        return 0.0
    return sum(1 for p, l in zip(preds, labels) if p == l) / len(preds)

