# Creating a project based on the Playground

In its simplest form, basing a project on the Playground is as simple as copying it.
If you want to have it auto-update, however, there is a bit more configuration needed.

## Creating a project based on the Playground

First, find the repository of the Playground you want to base your project on.
First-party playgrounds are provided in the [OpenSavvy GitLab group](https://gitlab.com/opensavvy/playgrounds).

On the top-right of the project page, find the fork button (you may need to log in before it appears).
Use it to create your own project in your own namespace—that's it!

## Basing an existing on the Playground

Open a terminal in the existing project, and execute the following commands:
```shell
git remote add playground CLONE_URL_OF_THE_PLAYGROUND_YOU_WANT_TO_USE

# The Playground will be merged into the current branch
# (use 'git switch' to use another branch)
git pull playground main
# If needed, fix conflicts etc.
```

That's it, this branch now uses the Playground. 

## Configuring the Playground to auto-update

This section assumes you are using GitLab.
You can certainly adapt our configuration for any other CI provider, but it is left as an exercise.

The Playground auto-updates by running a CI pipeline in which it pulls itself, then creating a merge request with the updated repository.
Because the merge request goes through CI, the repository will not be modified if there is any conflict or if the update breaks your CI.
If that happens, you can look through the created branch and fix it.

To do this, it needs:
- An SSH key authorized to push to the repository
- CI variables to tell it which Playground to pull
- A configured scheduled pipeline, which decides when it runs

First, create an SSH key:
```shell
ssh-keygen
```
When prompted to select a path, put it somewhere in which you won't confuse it with your own, e.g. `/tmp/id_rsa`.
After this configuration phase is over, you will not need it again, so there is no need to store it.

In your project's settings, go to Settings → Repository → Deploy keys.
Create a new key, name it however you want, and copy the contents of the public key (`/tmp/id_rsa.pub` if you used the same path as us) into the "Key" field. Tick "Grant write permissions to this key".

Now, still in your project's settings, go to Settings → CI/CD → Variables.
Create the following variables:
- `playground_parent` (type: variable): Clone URL of the playground you want to base yourself on
- `playground_parent_name` (type: variable): Name of the playground you are basing yourself on (will appear in the Git message)
- `playground_mirroring_key_public` (type: file, protected): Contents of the public key (`/tmp/id_rsa.pub` if you used the same path as us)
- `playground_mirroring_key` (type: file, protected): Contents of the private key (`/tmp/id_rsa` if you used the same path as us)

The mirroring job is now fully configured. However, it still needs to be triggered.
In the project sidebar, go to Build → Pipeline schedules.
Create a new schedule, select how often it runs.
We recommend once a week, but you can choose any frequency you want.

That's it, the Playground will now automatically update itself.
