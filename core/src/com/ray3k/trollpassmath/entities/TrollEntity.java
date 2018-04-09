/*
 * The MIT License
 *
 * Copyright 2018 Raymond Buckley.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.ray3k.trollpassmath.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.Event;
import com.ray3k.trollpassmath.Core;
import com.ray3k.trollpassmath.Entity;
import com.ray3k.trollpassmath.SpineTwoColorEntity;
import com.ray3k.trollpassmath.states.GameState;

public class TrollEntity extends SpineTwoColorEntity {

    public TrollEntity() {
        super(Core.DATA_PATH + "/spine/troll.json", "walk", GameState.twoColorPolygonBatch);
        
        getAnimationState().addListener(new AnimationState.AnimationStateAdapter() {
            @Override
            public void event(AnimationState.TrackEntry entry, Event event) {
                GameState.inst().playSound(event.getData().getName());
            }

            @Override
            public void end(AnimationState.TrackEntry entry) {
                if (entry.getAnimation().getName().equals("walk")) {
                    GameState.inst().createPerson();
                }
            }
            
        });
        
        setMotion(100.0f, 180.0f);
    }

    @Override
    public void actSub(float delta) {
        if (!MathUtils.isZero(getSpeed()) && getX() < GameState.GAME_WIDTH - 200.0f) {
            setMotion(0.0f, 180.0f);
            setX(GameState.GAME_WIDTH - 200.0f);
            getAnimationState().setAnimation(0, "stand", false);
        }
    }

    @Override
    public void drawSub(SpriteBatch spriteBatch, float delta) {
    }

    @Override
    public void create() {
    }

    @Override
    public void actEnd(float delta) {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void collision(Entity other) {
    }

}