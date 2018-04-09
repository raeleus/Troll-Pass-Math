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
package com.ray3k.trollpassmath.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.esotericsoftware.spine.utils.TwoColorPolygonBatch;
import com.ray3k.trollpassmath.Core;
import com.ray3k.trollpassmath.Entity;
import com.ray3k.trollpassmath.EntityManager;
import com.ray3k.trollpassmath.InputManager;
import com.ray3k.trollpassmath.State;
import com.ray3k.trollpassmath.entities.GameOverTimerEntity;
import com.ray3k.trollpassmath.entities.PersonEntity;
import com.ray3k.trollpassmath.entities.StaticEntity;
import com.ray3k.trollpassmath.entities.TrollEntity;

public class GameState extends State {
    private static GameState instance;
    private int score;
    private static int highscore = 0;
    private OrthographicCamera gameCamera;
    private Viewport gameViewport;
    private InputManager inputManager;
    private Skin skin;
    private Stage stage;
    public static EntityManager entityManager;
    public static TextureAtlas spineAtlas;
    public static TwoColorPolygonBatch twoColorPolygonBatch;
    public static final float GAME_WIDTH = 800;
    public static final float GAME_HEIGHT = 600;
    private float cloudCounter;
    private TrollEntity troll;
    private StaticEntity bridge;
    public Array<String> questions;
    public int lives;
    
    public static GameState inst() {
        return instance;
    }
    
    public GameState(Core core) {
        super(core);
    }
    
    @Override
    public void start() {
        instance = this;
        
        spineAtlas = Core.assetManager.get(Core.DATA_PATH + "/spine/Troll Pass.atlas", TextureAtlas.class);
        
        score = 0;
        
        inputManager = new InputManager();
        
        gameCamera = new OrthographicCamera();
        gameCamera.position.set(0.0f, 0.0f, 0.0f);
        gameViewport = new FillViewport(GAME_WIDTH, GAME_HEIGHT, gameCamera);
        gameViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        gameViewport.apply();
        
        skin = Core.assetManager.get(Core.DATA_PATH + "/ui/Troll-Pass-Math-UI.json", Skin.class);
        stage = new Stage(new ScreenViewport());
        
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(inputManager);
        inputMultiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(inputMultiplexer);
        
        entityManager = new EntityManager();
        
        twoColorPolygonBatch = new TwoColorPolygonBatch(3100);
        
        createStageElements();
        
        lives = 3;
        
        FileHandle file = Gdx.files.local(Core.DATA_PATH + "/data/data.txt");
        questions = new Array<String>(file.readString().split("\\n"));
        
        StaticEntity entity = new StaticEntity(Core.DATA_PATH + "/spine/bg.json", "animation", twoColorPolygonBatch);
        entityManager.addEntity(entity);
        
        bridge = new StaticEntity(Core.DATA_PATH + "/spine/bridge.json", "normal", twoColorPolygonBatch);
        bridge.setDepth(-100);
        entityManager.addEntity(bridge);
        
        entity = new StaticEntity(Core.DATA_PATH + "/spine/sun.json", "animation", twoColorPolygonBatch);
        entity.setDepth(100);
        entity.setPosition(465.0f, 491.0f);
        entityManager.addEntity(entity);
        
        for (int i = 0; i < 3; i++) {
            entity = new StaticEntity(Core.DATA_PATH + "/spine/cloud.json", "animation", twoColorPolygonBatch);
            entity.getSkeleton().setSkin("cloud" + MathUtils.random(1, 4));
            entity.setDepth(50);
            entity.setPosition(MathUtils.random(GAME_WIDTH), MathUtils.random(400.0f, 550.0f));
            entity.setMotion(MathUtils.random(2.5f, 20.0f), 180.0f);
            entityManager.addEntity(entity);
        }
        
        troll = new TrollEntity();
        troll.setPosition(GAME_WIDTH, 25.0f);
        entityManager.addEntity(troll);
    }
    
    private void generateCloud() {
        StaticEntity entity = new StaticEntity(Core.DATA_PATH + "/spine/cloud.json", "animation", twoColorPolygonBatch);
        entity.getSkeleton().setSkin("cloud" + MathUtils.random(1, 4));
        entity.setDepth(50);
        entity.setPosition(GAME_WIDTH + 200.0f, MathUtils.random(400.0f, 550.0f));
        entity.setMotion(MathUtils.random(2.5f, 20.0f), 180.0f);
        entityManager.addEntity(entity);
    }
    
    public void createPerson() {
        PersonEntity personEntity = new PersonEntity();
        entityManager.addEntity(personEntity);
    }
    
    public void smash() {
        troll.getAnimationState().setAnimation(0, "smash", false);
        bridge.getAnimationState().setAnimation(0, "shake", false);
        
        for (Entity entity : entityManager.getEntities()) {
            if (entity instanceof PersonEntity) {
                PersonEntity person = (PersonEntity) entity;
                person.getAnimationState().setAnimation(0, "die", false);
                break;
            }
        }

        loseLife();
    }
    
    public void pass() {
        troll.getAnimationState().setAnimation(0, "pass", false);
        addScore(1);
        
        for (Entity entity : entityManager.getEntities()) {
            if (entity instanceof PersonEntity) {
                PersonEntity person = (PersonEntity) entity;
                person.getAnimationState().setAnimation(0, "walk", true);
                person.getAnimationState().setAnimation(1, "leave", false);
                break;
            }
        }
    }
    
    public void showQuestion() {
        String question = questions.random();
        
        if (question != null) {
            questions.removeValue(question, false);

            Label label = stage.getRoot().findActor("question");
            label.setText(question.replaceAll("\\s*=.*", ""));

            stage.getRoot().findActor("answer").setUserObject(question.replaceAll(".*\\s*=\\s*|\\s*$", ""));

            stage.getRoot().findActor("question").addAction(Actions.fadeIn(.5f));
            
            TextField textField = stage.getRoot().findActor("answer");
            textField.addAction(Actions.sequence(Actions.touchable(Touchable.enabled), Actions.fadeIn(.5f)));
            textField.setText("");
            stage.setKeyboardFocus(textField);
            
            stage.getRoot().findActor("ok").addAction(Actions.sequence(Actions.fadeIn(.5f), Actions.touchable(Touchable.enabled)));
        } else {
            entityManager.addEntity(new GameOverTimerEntity(2.0f));
        }
    }
    
    private void evaluateAnswer() {
        stage.getRoot().findActor("question").addAction(Actions.fadeOut(.5f));
        
        TextField textField = stage.getRoot().findActor("answer");
        textField.addAction(Actions.sequence(Actions.touchable(Touchable.disabled), Actions.fadeOut(.5f)));
        
        stage.getRoot().findActor("ok").addAction(Actions.sequence(Actions.touchable(Touchable.disabled), Actions.fadeOut(.5f)));

        if (textField.getText().equals(textField.getUserObject())) {
            pass();
        } else {
            smash();
        }
    }
    
    private boolean loseLife() {
        lives--;
        setLives(lives);
        if (lives <= 0) {
            entityManager.addEntity(new GameOverTimerEntity(2.0f));
            return false;
        } else {
            return true;
        }
    }
    
    private void setLives(int lives) {
        Table table = stage.getRoot().findActor("lives");
        
        table.clear();
        
        for (int i = 0; i < lives; i++) {
            Image image = new Image(skin, "life");
            image.setScaling(Scaling.none);
            table.add(image);
        }
    }
    
    private void createStageElements() {
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);
        
        Label label = new Label("This is a test", skin, "small");
        label.setName("question");
        label.setColor(1.0f, 1.0f, 1.0f, 0.0f);
        root.add(label).padTop(25.0f);
        
        root.row();
        Table table = new Table();
        root.add(table).expandY().expandY().top();
        
        final TextField textField = new TextField("", skin);
        textField.setName("answer");
        textField.setColor(1.0f, 1.0f, 1.0f, 0.0f);
        table.add(textField);
        
        textField.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Keys.ENTER && textField.getTouchable() == Touchable.enabled) {
                    evaluateAnswer();
                }
                return true;
            }
        });
        
        TextButton textButton = new TextButton("OK", skin, "small");
        textButton.setName("ok");
        textButton.setColor(1.0f, 1.0f, 1.0f, 0.0f);
        table.add(textButton).space(20.0f);
        
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                evaluateAnswer();
            }
        });
        
        root.row();
        label = new Label("0", skin);
        label.setName("score");
        root.add(label);
        
        root = new Table();
        root.setFillParent(true);
        stage.addActor(root);
        
        table = new Table();
        table.setName("lives");
        root.add(table).expand().bottom().left();
        
        setLives(3);
    }
    
    @Override
    public void draw(SpriteBatch spriteBatch, float delta) {
        Gdx.gl.glClearColor(192 / 255.0f, 255 / 255.0f, 253 / 255.0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        gameCamera.update();
        twoColorPolygonBatch.setProjectionMatrix(gameCamera.combined);
        twoColorPolygonBatch.begin();
        twoColorPolygonBatch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
        entityManager.draw(spriteBatch, delta);
        twoColorPolygonBatch.end();
        
        stage.draw();
    }

    @Override
    public void act(float delta) {
        entityManager.act(delta);
        
        cloudCounter -= delta;
        
        if (cloudCounter < 0) {
            cloudCounter = MathUtils.random(10.0f, 20.0f);
            
            generateCloud();
        }
        
        stage.act(delta);
        
        if (Gdx.input.isKeyPressed(Keys.ESCAPE)) {
            Core.stateManager.loadState("menu");
        }
    }

    @Override
    public void dispose() {
        if (twoColorPolygonBatch != null) {
            twoColorPolygonBatch.dispose();
        }
    }

    @Override
    public void stop() {
        stage.dispose();
    }
    
    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height, true);
        stage.getViewport().update(width, height, true);
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
        if (score > highscore) {
            highscore = score;
        }
        
        Label label = stage.getRoot().findActor("score");
        label.setText(Integer.toString(this.score));
    }
    
    public void addScore(int score) {
        this.score += score;
        if (this.score > highscore) {
            highscore = this.score;
        }
        
        Label label = stage.getRoot().findActor("score");
        label.setText(Integer.toString(this.score));
    }

    public OrthographicCamera getGameCamera() {
        return gameCamera;
    }

    public void setGameCamera(OrthographicCamera gameCamera) {
        this.gameCamera = gameCamera;
    }

    public Skin getSkin() {
        return skin;
    }

    public Stage getStage() {
        return stage;
    }
    
    public void playSound(String name) {
        playSound(name, 1.0f, 1.0f);
    }
    
    public void playSound (String name, float volume) {
        playSound(name, volume, 1.0f);
    }
    
    /**
     * 
     * @param name
     * @param volume
     * @param pitch .5 to 2. 1 is default
     */
    public void playSound(String name, float volume, float pitch) {
        Core.assetManager.get(Core.DATA_PATH + "/sfx/" + name + ".wav", Sound.class).play(volume, pitch, 0.0f);
    }
}