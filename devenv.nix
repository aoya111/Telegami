{ pkgs, lib, config, inputs, ... }: {
  # https://devenv.sh/packages/
  packages = with pkgs; [ gh git gradle ];

  # https://devenv.sh/languages/
  # android = {
  #   enable = true;
  #   platforms.version = [ "35" ];
  #   buildTools.version = [ "35.0.0" ];
  #   emulator = { enable = true; };
  # };
  languages = {
    kotlin.enable = true;
    python = {
      enable = true;
      version = "3.12";
      venv.enable = true;
      uv.enable = true;
    };
  };

  # https://devenv.sh/processes/
  # processes.cargo-watch.exec = "cargo-watch";

  # https://devenv.sh/services/
  # services.postgres.enable = true;

  enterShell = ''
    export ANDROID_HOME=$HOME/android-sdk
    export PATH=$ANDROID_HOME/cmdline-tools/latest/bin:$PATH
    export PATH=$ANDROID_HOME/platform-tools:$PATH
    export PATH=$ANDROID_HOME/emulator:$PATH
    export PATH=$ANDROID_HOME/ndk:$PATH

    git --version
  '';

  # https://devenv.sh/tasks/
  # tasks = {
  #   "myproj:setup".exec = "mytool build";
  #   "devenv:enterShell".after = [ "myproj:setup" ];
  # };

  # https://devenv.sh/tests/
  enterTest = ''
    echo "Running tests"
    git --version | grep --color=auto "${pkgs.git.version}"
  '';

  # https://devenv.sh/git-hooks/
  # git-hooks.hooks.shellcheck.enable = true;

  # See full reference at https://devenv.sh/reference/options/
}
