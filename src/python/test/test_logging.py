"""
Unit tests for basic network logging
"""

import numpy as np
from ..nengo_deco import nengo_log

@nengo_log
def connect_something(logfile, orig=True, mode=None, seed=None):
    import nef
    net = nef.Network('top')
    netA = nef.Network('A')
    netB = nef.Network('B')
    netA.make('X', 100, 1)
    netA.make('Y', 100, 1)
    netB.make('X', 100, 1)
    netB.make('Y', 100, 1)
    net.add(netA.network)
    net.add(netB.network)
    net.connect('A.X', 'B.Y')
    #
    log = nef.log.TimelockedLog(network=net, filename=logfile)
    log.add('A.X')
    log.add('A.Y')
    log.add('B.X')
    log.add('B.Y')

    if orig:
        return net
    else:
        net.add_to_nengo()
        jython_src = net.network.dumpToScript()
        # -- autogenerated code names the toplevel thing 'net'
        exec jython_src
        return net


def test_sane_stats():
    stats0 = connect_something(orig=True, t=1.0, seed=123, mode='spiking')
    assert np.allclose(stats0['time'], np.arange(0.0, 1.0, .001)[:, None])
    assert len(stats0['A.X']) == 1000
    assert len(stats0['A.Y']) == 1000
    assert len(stats0['B.X']) == 1000
    assert len(stats0['B.Y']) == 1000
    assert np.all(np.var(stats0['A.X']) > 0)


def test_reproducible_small():
    stats0 = connect_something(orig=True, t=1.0, seed=123, mode='spiking')
    stats1 = connect_something(orig=True, t=1.0, seed=123, mode='spiking')
    assert np.all(stats0 == stats1)


def test_reproducible_large():
    # -- simulate a large model with lots of chances for accidentally
    #    re-seeding things
    raise NotImplementedError()
    stats0 == 0
    stats1 == 0
    assert np.all(stats0 == stats1)


def test_log_tau():
    raise NotImplementedError()


def test_seed_doesnt_matter_very_much():
    stats0 = connect_something(orig=True, t=1.0, seed=123, mode='spiking')
    stats1 = connect_something(orig=True, t=1.0, seed=124, mode='spiking')
    assert not np.all(stats0 == stats1)
    assert abs(np.var(stats0) - np.var(stats1))


def test_seed():
    stats0 = connect_something(orig=True, t=1.0, seed=123, mode='direct')
    stats1 = connect_something(orig=False, t=1.0, seed=123, mode='direct')
    assert stats0 == stats1


